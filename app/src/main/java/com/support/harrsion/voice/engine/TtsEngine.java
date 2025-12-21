package com.support.harrsion.voice.engine;

import com.support.harrsion.dto.xtts.XTTSParams;

public interface TtsEngine {

    interface Callback {
        void onAudio(byte[] pcm);
        void onComplete();
        void onError(int code, String msg);
    }

    void start(String text, XTTSParams params);
    void stop();
}
