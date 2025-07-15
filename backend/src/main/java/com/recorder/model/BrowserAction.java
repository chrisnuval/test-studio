package com.recorder.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class BrowserAction {
    private String action;
    private String selector;
    private String value;
    private String tag;
    private String text;
    private String findBy;
    private String name;
    private Map<String, String> attributes;

    public BrowserAction() {}
    @Override
    public String toString() {
        return String.format("Action: %s, Selector: %s, Value: %s, Tag: %s, Text: %s",
                action, selector, value, tag, text);
    }
}