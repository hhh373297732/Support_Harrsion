package com.support.harrsion.voice.input;

public interface AudioInput {

    interface Callback {
        void onAudioFrame(byte[] data, int length);
        void onError(Exception e);
    }

    void start(Callback callback);
    void stop();
}
