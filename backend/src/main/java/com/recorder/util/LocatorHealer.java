package com.recorder.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recorder.config.AIModelConfig;
import com.recorder.model.AIRequest;
import com.recorder.model.BrowserAction;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@ApplicationScoped
@Slf4j
public class LocatorHealer {

    @Inject
    AIClient aiClient;

    @Inject
    AIModelConfig aiModelConfig;

    public String tryHealing(BrowserAction action, WebDriver driver) {
        // Heuristic
//        String heuristicSelector = heuristicGuess(action);
//        if (heuristicSelector != null && elementExists(driver, heuristicSelector)) {
//            log.info("Heuristic:Healing of " + action + " with selector " + heuristicSelector);
//            return heuristicSelector;
//        }

        // AI fallback
        String aiSuggestion = suggestViaAI(action);
        log.info("AI Suggestion: {}", aiSuggestion);
        if (aiSuggestion != null && elementExists(driver, aiSuggestion)) {
            log.info("AI:Healing of " + action + " with selector " + aiSuggestion);
            return aiSuggestion;
        }

        return null;
    }

    private boolean elementExists(WebDriver driver, String selector) {
        try {
            return driver.findElements(By.cssSelector(selector)).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private String heuristicGuess(BrowserAction action) {
        if (action.getName() != null && !action.getName().isEmpty()) {
            return "[name='" + action.getName() + "']";
        }
        if (action.getTag() != null && !action.getTag().isEmpty()) {
            return action.getTag().toLowerCase();
        }
        return null;
    }

    private String suggestViaAI(BrowserAction action) {
        try {
            StringBuilder prompt = new StringBuilder("Suggest the best CSS selector for an element with these attributes and dont provide any other explanation just the css selector path generated and provide the answer inside ``` ```:\n");
            prompt.append("Tag: ").append(action.getTag()).append("\n");
            prompt.append("Text: ").append(action.getText()).append("\n");
            prompt.append("Name: ").append(action.getName()).append("\n");

            if (action.getAttributes() != null && !action.getAttributes().isEmpty()) {
                prompt.append("Other attributes:\n");
                action.getAttributes().forEach((key, value) ->
                        prompt.append("- ").append(key).append(": ").append(value).append("\n"));
            }
            log.info("Prompt generated: {}", prompt.toString());
            AIRequest request = new AIRequest(aiModelConfig.getModelName(), prompt.toString(), false);
            String raw = aiClient.sendToAI(request);
            JsonNode json = new ObjectMapper().readTree(raw);
            String responseText = json.get("response").asText().trim();
            log.info("AI Suggestion before extraction: " + responseText);
            return extractSelector(responseText);
        } catch (Exception e) {
            return null;
        }
    }

    public String generateFriendlyNameViaAI(BrowserAction action) {
        try {
            StringBuilder prompt = new StringBuilder("Generate a short, human-readable name for a web element with the following details. Don't include any explanation, just return the name:\n");
            prompt.append("Tag: ").append(action.getTag()).append("\n");
            prompt.append("Text: ").append(action.getText()).append("\n");
            prompt.append("Name attribute: ").append(action.getName()).append("\n");

            if (action.getAttributes() != null && !action.getAttributes().isEmpty()) {
                prompt.append("Other attributes:\n");
                action.getAttributes().forEach((key, value) -> {
                    if (!key.startsWith("_ngcontent") && !"loading".equalsIgnoreCase(key)) {
                        prompt.append("- ").append(key).append(": ").append(value).append("\n");
                    }
                });
            }

            AIRequest request = new AIRequest(aiModelConfig.getModelName(), prompt.toString(), false);
            String raw = aiClient.sendToAI(request);
            JsonNode json = new ObjectMapper().readTree(raw);
            return json.get("response").asText().trim();
        } catch (Exception e) {
            return "Unnamed Element";
        }
    }

    private String extractSelector(String raw) {
        //Extracting css block
        if (raw.contains("```")) {
            int start = raw.indexOf("```");
            int end = raw.indexOf("```", start + 1);
            if (start != -1 && end != -1 && start < end) {
                return raw.substring(start + 6, end).trim(); // 6 = length of "```css"
            }
        }

        for (String line : raw.split("\n")) {
            line = line.trim();
            if (!line.isEmpty() && (line.contains(".") || line.contains("#") || line.contains(":") || line.contains(">"))) {
                return line;
            }
        }
        return null;
    }
}
