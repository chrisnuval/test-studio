package com.recorder.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recorder.config.AIModelConfig;
import com.recorder.model.AIRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class AIClient {

    @Inject
    AIModelConfig aiModelConfig;

    public String sendToAI(AIRequest request) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(request);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(aiModelConfig.getEndpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
