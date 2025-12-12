package com.support.harrsion.dto.agent;

import com.support.harrsion.config.Prompts;

import lombok.Data;

@Data
public class AgentConfig {

    private int maxSteps = 100;
    private String systemPrompt;
    private Boolean verbose = true;

    public AgentConfig() {
        this.systemPrompt = Prompts.buildSystemPrompt();
    }
}
