package com.support.harrsion.voice.vad;

import com.support.harrsion.voice.VoiceState;

public interface VoiceActivityDetector {

    interface Callback {
        void onVoiceState(
                VoiceState state,
                byte[] audio,
                int length
        );
    }

    void onAudioFrame(byte[] data, int length);
    void setCallback(Callback callback);
    void reset();
}
