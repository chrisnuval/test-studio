package com.recorder.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrowserAction {
    public String action;
    public String selector;
    public String value;
    public String tag;
    public String text;
    public String findBy;

    public BrowserAction() {}
    @Override
    public String toString() {
        return String.format("Action: %s, Selector: %s, Value: %s, Tag: %s, Text: %s",
                action, selector, value, tag, text);
    }
}