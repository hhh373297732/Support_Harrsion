package com.support.harrsion.voice.engine;

import com.support.harrsion.voice.VoiceState;

public interface IvwEngine {

    interface Callback {
        void onWakeUp(String result);
        void onError(String msg);
    }

    void setCallback(Callback callback);

    void start();
    void onVoice(
            VoiceState state,
            byte[] audio,
            int length
    );
    void stop();
}
