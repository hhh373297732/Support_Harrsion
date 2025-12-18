package com.support.harrsion.manager;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class TTSAudioPlayer {
    private static final int SAMPLE_RATE = 16000;
    private AudioTrack audioTrack;
    private HandlerThread handlerThread;
    private Handler audioHandler;

    public TTSAudioPlayer() {
        handlerThread = new HandlerThread("TTS_Player_Thread");
        handlerThread.start();
        audioHandler = new Handler(handlerThread.getLooper());
    }

    public void init() {
        audioHandler.post(() -> {
            int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.play();
        });
    }

    public void write(byte[] data) {
        audioHandler.post(() -> {
            if (audioTrack == null) return;

            // 调试日志
            Log.d("TTSPlayer", "Writing bytes: " + data.length + " State: " + audioTrack.getPlayState());
            if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                audioTrack.play();
            }

            // 写入数据
            audioTrack.write(data, 0, data.length);
        });
    }

    public void stop() {
        audioHandler.post(() -> {
            if (audioTrack != null) {
                audioTrack.pause();
                audioTrack.flush();
            }
        });
    }

    public void release() {
        if (handlerThread != null) {
            handlerThread.quitSafely();
        }
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
    }
}