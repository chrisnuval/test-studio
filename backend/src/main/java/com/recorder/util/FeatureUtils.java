package com.recorder.util;

import com.recorder.model.BrowserAction;
import com.recorder.model.Locator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureUtils {
    public static String createFeatureFromActions(List<BrowserAction> actions, List<Locator> locators) {
        StringBuilder sb = new StringBuilder();
        sb.append("Feature: Recorded User Interaction\n");
        sb.append("  Scenario: User interaction playback\n");

        Set<String> typedKeys = new HashSet<>();

        for (BrowserAction action : actions) {
            String readableName = action.getSelector(); // fallback to raw selector

            // 🔍 Match locator name based on selector
            for (Locator locator : locators) {
                if (locator.getLookupDetails() != null &&
                        locator.getLookupDetails().getValue().equals(action.getSelector())) {
                    readableName = locator.getName();
                    break;
                }
            }

            String step = "";
            switch (action.getAction().toLowerCase()) {
                case "click":
                    step = String.format("    When I click on \"%s\"", readableName);
                    break;
                case "input":
                    String key = action.getSelector() + "::" + action.getValue();
                    if (typedKeys.contains(key)) continue;
                    typedKeys.add(key);
                    step = String.format("    When I enter '%s' in '%s'", action.getValue(), readableName);
                    break;
                default:
                    // Add other action types if needed
                    break;
            }

            if (!step.isEmpty()) {
                sb.append(step).append("\n");
            }
        }

        return sb.toString();
    }


    public static String cleanText(String raw) {
        return raw.replaceAll("(?m)^\\s+", "").replaceAll(" {2,}", " ").trim();
    }
}
