package com.support.harrsion.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepResult {
    private Boolean success;
    private Boolean finished;
    private Map<String, Object> action;
    private String thinking;
    private String message;
}
