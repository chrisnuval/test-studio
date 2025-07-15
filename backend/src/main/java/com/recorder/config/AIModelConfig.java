package com.recorder.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AIModelConfig {
    @Inject
    @Getter
    @ConfigProperty(name = "ai.model", defaultValue = "codellama")
    String modelName;

    @Inject
    @Getter
    @ConfigProperty(name = "ai.endpoint", defaultValue = "http://localhost:11434/api/generate")
    String endpoint;
}
