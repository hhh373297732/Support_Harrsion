package com.support.harrsion.voice.manager;

import android.util.Log;

import com.support.harrsion.voice.input.AudioInput;
import com.support.harrsion.voice.vad.VoiceActivityDetector;
import com.support.harrsion.voice.VoiceState;
import com.support.harrsion.voice.engine.IvwEngine;

public class IVWManager implements AudioInput.Callback, VoiceActivityDetector.Callback {

    private final AudioInput audioInput;
    private final VoiceActivityDetector vad;
    private final IvwEngine engine;

    public IVWManager(
            AudioInput audioInput,
            VoiceActivityDetector vad,
            IvwEngine engine
    ) {
        this.audioInput = audioInput;
        this.vad = vad;
        this.engine = engine;

        vad.setCallback(this);
    }

    public void start() {
        engine.start();
        audioInput.start(this);
    }

    public void stop() {
        audioInput.stop();
        engine.stop();
        vad.reset();
    }

    @Override
    public void onAudioFrame(byte[] data, int length) {
        vad.onAudioFrame(data, length);
    }

    @Override
    public void onError(Exception e) {
        Log.e("IVWManager", "AudioInput Error", e);
    }

    @Override
    public void onVoiceState(
            VoiceState state,
            byte[] audio,
            int length
    ) {
        engine.onVoice(state, audio, length);
    }
}
