package com.support.harrsion.dto.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserMessageOption {
    private String text;
    private String imageBase64;

}
