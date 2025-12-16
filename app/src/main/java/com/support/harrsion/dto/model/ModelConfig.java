package com.support.harrsion.dto.model;

import lombok.Data;

@Data
public class ModelConfig {
    private String baseUrl = "http://localhost:8000/v1";
    private String apiKey = "2611b5cb295546cf97233a9e5d05c6f3.nMrCYYl6I3eQTX34";
    private String modelName = "autoglm-phone-9b";
    private long maxTokens = 3000;
    private float temperature = 0.0f;
    private float topP = 0.85f;
    private float frequencyPenalty = 0.2f;
}
