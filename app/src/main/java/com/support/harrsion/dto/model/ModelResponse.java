package com.support.harrsion.dto.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelResponse {
    private String thinking;
    private String action;
    private String rawContent;
}
