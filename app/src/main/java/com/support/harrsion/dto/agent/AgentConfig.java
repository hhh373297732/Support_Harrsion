package com.support.harrsion.dto.agent;

import com.support.harrsion.config.Prompts;

import lombok.Data;

@Data
public class AgentConfig {

    private int maxSteps = 100;
    private Boolean verbose = true;
    private String systemPrompt;

    public AgentConfig() {
        this.systemPrompt = Prompts.buildSystemPrompt();
    }

    public AgentConfig(int maxSteps, Boolean verbose) {
        this.verbose = verbose;
        this.maxSteps = maxSteps;
        this.systemPrompt = Prompts.buildSystemPrompt();
    }
}
