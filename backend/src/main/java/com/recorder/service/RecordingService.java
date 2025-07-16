package com.recorder.service;

import com.recorder.config.AppConfig;
import com.recorder.driver.WebDriverFactory;
import com.recorder.model.BrowserAction;
import com.recorder.model.Locator;
import com.recorder.util.LocatorHealer;
import com.recorder.util.RecorderScriptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class RecordingService {

    @Inject
    LocatorHealerService locatorHealerService;

    @Inject
    LocatorHealer locatorHealer;

    private static final List<BrowserAction> actions = new ArrayList<>();
    private static String recordedUrl = AppConfig.BASE_URL;

    public Response start(String url) {
        clear();
        LocatorService.clear();

        if (url == null || url.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("URL is missing").build();
        }

        WebDriver driver = WebDriverFactory.createDriver();
        recordedUrl = url != null ? url : AppConfig.BASE_URL;
        try {
            driver.get(recordedUrl);
            ((JavascriptExecutor) driver).executeScript(RecorderScriptUtil.getRecorderScript());
            log.info("Recorder script injected.");
            return Response.ok("Recording started at: " + url).build();
        } catch (Exception e) {
            log.error("Error injecting script or navigating", e);
            WebDriverFactory.quitDriver();
            return Response.serverError().entity("Recording error: " + e.getMessage()).build();
        }
    }

    public Response playback() {
        List<BrowserAction> actions = getActions();
        WebDriver driver = WebDriverFactory.createDriver();
        driver.get(recordedUrl);

        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            for (BrowserAction action : actions) {
                try {
                    log.info("➡️ Attempting action: '{}' on selector: [{}] using '{}'", action.getAction(), action.getSelector(), action.getFindBy());
                    if ("highlight".equalsIgnoreCase(action.getAction())) {
                        log.info("Transforming 'highlight' action to 'assert' for selector: {}", action.getSelector());
                        String originalKey = action.getSelector();

                        action.setAction("assert");

                        LocatorService.removeLocator(originalKey);
                        LocatorService.addLocator(new Locator(
                                action.getName() != null ? action.getName() : action.getSelector(),
                                new Locator.LookupDetails(action.getFindBy(), action.getSelector())
                        ));
                    }
                    By locator = getLocatorFromAction(action);
                    WebElement el = wait.until(ExpectedConditions.elementToBeClickable(locator));
                    performAction(el, action, driver);
                } catch (Exception e) {
                    log.warn("Standard locator failed for: {}", action.getSelector());
                    String originalKey = action.getSelector();
                    Optional<WebElement> healed = locatorHealerService.heal(driver, action);
                    if (healed.isPresent()) {
                        performAction(healed.get(), action, driver);

                        LocatorService.removeLocator(originalKey); //removed failing locator after healing
                        log.info("Healed and performed action on: {}", action.getSelector());
                        String aiName = locatorHealer.generateFriendlyNameViaAI(action);
                        LocatorService.addLocator(new Locator(
                                aiName,
                                new Locator.LookupDetails(action.getFindBy(), action.getSelector())
                        ));
                    } else {
                        log.error("Could not heal locator for: {}", action.getSelector());
                    }
                }

                Thread.sleep(500); // add delay for realism/stability
            }

        } catch (Exception e) {
            log.error("Playback error", e);
            return Response.serverError().entity("Playback error: " + e.getMessage()).build();
        } finally {
            WebDriverFactory.quitDriver();
        }

        return Response.ok("Playback complete").build();
    }

    private String generateLocatorName(String selector, WebElement element) {
        try {
            String name = element.getAttribute("name");
            if (name != null && !name.isBlank()) return name;

            String alt = element.getAttribute("alt");
            if (alt != null && !alt.isBlank()) return alt;

            String placeholder = element.getAttribute("placeholder");
            if (placeholder != null && !placeholder.isBlank()) return placeholder;

            String text = element.getText();
            if (text != null && !text.isBlank()) return text;

            return selector;
        } catch (Exception e) {
            return selector;
        }
    }

    private By getLocatorFromAction(BrowserAction action) {
        String findBy = action.getFindBy();
        if (findBy == null) findBy = "cssSelector";

        switch (findBy) {
            case "id":
                return By.id(action.getSelector());
            case "name":
                return By.name(action.getSelector());
            case "xpath":
                return By.xpath(action.getSelector());
            case "cssSelector":
            default:
                return By.cssSelector(action.getSelector());
        }
    }

    private void performAction(WebElement el, BrowserAction action, WebDriver driver) {
        switch (action.getAction().toLowerCase()) {
            case "click" -> el.click();
            case "input" -> {
                el.clear();
                el.sendKeys(action.getValue());
            }
            case "assert" -> {
                log.info("Asserting visibility of element: {}", action.getSelector());
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
                wait.until(ExpectedConditions.visibilityOf(el));
                log.info("✅ Element is visible: {}", action.getSelector());
            }
        }
    }

    public Response stop() {
        WebDriverFactory.quitDriver();
        return Response.ok(new ArrayList<>(actions)).build();
    }

    public Response log(BrowserAction action) {
        actions.add(action);
        return Response.ok().build();
    }

    public static void clear() {
        actions.clear();
    }

    public static List<BrowserAction> getActions() {
        return actions;
    }
}
