package com.support.harrsion.dto.screenshot;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Screenshot {
    private String base64Data;
    private int width;
    private int height;
    private Boolean isSensitive;
}
