package com.support.harrsion.voice.pipeline;

import com.support.harrsion.dto.xtts.XTTSParams;

public interface VoicePipeline {

    void start();
    void stop();

    /** 主动播报（不一定来自唤醒） */
    void speak(String text, XTTSParams params);
}

