package com.support.harrsion.dto.xtts;

import lombok.Data;

@Data
public class XTTSParams {

    public String vcn = "xiaoyan";
    public int language =1 ;
    public int pitch = 50;
    public int speed = 50;
    public int volume = 50;
    public String textEncoding = "UTF-8";
}
