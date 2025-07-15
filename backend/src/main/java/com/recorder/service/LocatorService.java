package com.recorder.service;

import com.recorder.model.BrowserAction;
import com.recorder.model.Locator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;

import java.util.*;

@ApplicationScoped
public class LocatorService {

    private static final List<Locator> LOCATORS = new ArrayList<>();

    public static void addLocator(Locator locator) {
        LOCATORS.add(locator);
    }

    public static void clear() {
        LOCATORS.clear();
    }

    public List<Locator> getRawLocators() {
        return new ArrayList<>(LOCATORS);
    }

    public static void removeLocator(String selectorToRemove) {
        LOCATORS.removeIf(locator ->
                locator.getLookupDetails().getValue().equals(selectorToRemove)
        );
    }

    public static List<Locator> getDeduplicatedLocatorsFromActions(List<BrowserAction> actions) {
        Set<String> seen = new HashSet<>();
        List<Locator> result = new ArrayList<>();

        for (BrowserAction action : actions) {
            String selector = action.getSelector();
            if (selector == null || selector.isBlank()) continue;

            String name = action.getTag() != null ? action.getTag() : "element";
            String key = name + "::cssSelector::" + selector;

            if (seen.add(key)) {
                Locator locator = new Locator();
                locator.setName(name);
                Locator.LookupDetails details = new Locator.LookupDetails("cssSelector", selector);
                locator.setLookupDetails(details);
                result.add(locator);
            }
        }

        return result;
    }
}
