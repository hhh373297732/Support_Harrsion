package com.support.harrsion.voice.flow;

import com.support.harrsion.dto.xtts.XTTSParams;
import com.support.harrsion.voice.manager.XTTSManager;

public class TtsFlow {

    private final XTTSManager ttsManager;

    public TtsFlow(XTTSManager ttsManager) {
        this.ttsManager = ttsManager;
    }

    public void speak(String text, XTTSParams params) {
        ttsManager.speak(text, params);
    }

    public void stop() {
        ttsManager.stop();
    }
}
