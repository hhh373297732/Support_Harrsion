package com.support.harrsion.dto.action;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionResult {
    private Boolean success;
    private Boolean shouldFinish;
    private String message;
    private Boolean requiresConfirmation;
}
