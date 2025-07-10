package com.recorder.util;

public class RecorderScriptUtil {

    public static String getRecorderScript() {
        return """
        (function() {
          function getBestLocator(el) {
            if (el.id && document.querySelectorAll('#' + el.id).length === 1) {
              return { findBy: 'id', value: el.id };
            }
            if (el.name && document.querySelectorAll('[name="' + el.name + '"]').length === 1) {
              return { findBy: 'name', value: el.name };
            }
            try {
              const cssPath = getCssPath(el);
              if (document.querySelectorAll(cssPath).length === 1) {
                return { findBy: 'cssSelector', value: cssPath };
              }
            } catch (_) {}

            return { findBy: 'xpath', value: getXPath(el) };
          }

          function getCssPath(el) {
            if (!(el instanceof Element)) return;
            const path = [];
            while (el.nodeType === Node.ELEMENT_NODE) {
              let selector = el.nodeName.toLowerCase();
              if (el.id) {
                selector += '#' + el.id;
                path.unshift(selector);
                break;
              } else {
                let sib = el, nth = 1;
                while ((sib = sib.previousElementSibling)) {
                  if (sib.nodeName.toLowerCase() === selector) nth++;
                }
                selector += ":nth-of-type(" + nth + ")";
              }
              path.unshift(selector);
              el = el.parentNode;
            }
            return path.join(" > ");
          }

          function getXPath(el) {
            if (el.id) return '//*[@id="' + el.id + '"]';
            if (el === document.body) return '/html/body';
            let ix = 0;
            const siblings = el.parentNode ? el.parentNode.childNodes : [];
            for (let i = 0; i < siblings.length; i++) {
              const sibling = siblings[i];
              if (sibling === el) return getXPath(el.parentNode) + '/' + el.tagName.toLowerCase() + '[' + (ix + 1) + ']';
              if (sibling.nodeType === 1 && sibling.tagName === el.tagName) ix++;
            }
          }

          function send(action, el, value = '') {
            const locator = getBestLocator(el);
            const payload = {
              action: action,
              selector: locator.value,
              value: value,
              tag: el.tagName,
              text: el.innerText || '',
              name: el.getAttribute("name") || el.getAttribute("aria-label") || el.getAttribute("placeholder") || '',
              findBy: locator.findBy
            };

            fetch('http://localhost:8080/record/log', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });

            if (action === 'input' || action === 'click') {
              fetch('http://localhost:8080/record/locator', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                  name: payload.name || payload.selector,
                  lookupDetails: {
                    findBy: payload.findBy,
                    value: payload.selector
                  }
                })
              });
            }
          }

          document.addEventListener('click', function(e) {
            send('click', e.target);
          });

          document.addEventListener('change', function(e) {
            send('input', e.target, e.target.value);
          });

          console.log('📡 Recorder injected and running...');
        })();
        """;
    }
}