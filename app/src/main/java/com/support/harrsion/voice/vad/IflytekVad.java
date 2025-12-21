package com.support.harrsion.voice.vad;

import com.support.harrsion.voice.VoiceState;

public class IflytekVad implements VoiceActivityDetector {

    private static final double DEFAULT_THRESHOLD = 60.0;
    private static final long DEFAULT_SILENCE_MS = 500;

    private final double threshold;
    private final long silenceHoldMs;

    private long lastVoiceTime = 0;
    private boolean isSpeaking = false;

    private Callback callback;

    public IflytekVad() {
        this(DEFAULT_THRESHOLD, DEFAULT_SILENCE_MS);
    }

    public IflytekVad(double threshold, long silenceHoldMs) {
        this.threshold = threshold;
        this.silenceHoldMs = silenceHoldMs;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onAudioFrame(byte[] data, int length) {
        double db = calculateVolume(data, length);
        long now = System.currentTimeMillis();
        boolean hasVoice = db > threshold;

        if (hasVoice) {
            lastVoiceTime = now;
        }

        if (hasVoice || now - lastVoiceTime < silenceHoldMs) {

            if (!isSpeaking) {
                isSpeaking = true;
                notifyState(VoiceState.VOICE_START, data, length);
            } else {
                notifyState(VoiceState.VOICE_ACTIVE, data, length);
            }

        } else {
            if (isSpeaking) {
                isSpeaking = false;
                notifyState(VoiceState.VOICE_END, data, length);
            } else {
                notifyState(VoiceState.SILENCE, data, length);
            }
        }
    }

    @Override
    public void reset() {
        isSpeaking = false;
        lastVoiceTime = 0;
    }

    private void notifyState(VoiceState state, byte[] data, int length) {
        if (callback != null) {
            callback.onVoiceState(state, data, length);
        }
    }

    private double calculateVolume(byte[] buffer, int readSize) {
        long v = 0;
        for (int i = 0; i < readSize; i += 2) {
            int value = (short) ((buffer[i] & 0xFF) | (buffer[i + 1] << 8));
            v += value * value;
        }
        double mean = v / (readSize / 2.0);
        return 10 * Math.log10(mean);
    }
}
