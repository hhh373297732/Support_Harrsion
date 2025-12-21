package com.support.harrsion.voice.output;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class AudioTrackOutput implements AudioOutput {

    private static final int SAMPLE_RATE = 16000;

    private AudioTrack audioTrack;
    private HandlerThread thread;
    private Handler handler;
    private volatile boolean released = false;

    @Override
    public void start() {
        if (thread != null) return;

        thread = new HandlerThread("AudioOutput");
        thread.start();
        handler = new Handler(thread.getLooper());

        handler.post(() -> {
            int min = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    Math.max(min, 4096),
                    AudioTrack.MODE_STREAM
            );
            audioTrack.play();
        });
    }

    @Override
    public void write(byte[] pcm) {
        Log.d("AudioTrackOutput", "write");
        if (released || handler == null) return;

        handler.post(() -> {
            if (audioTrack != null) {
                // 【新增】如果处于暂停状态，重新开始播放
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.play();
                }
                audioTrack.write(pcm, 0, pcm.length);
            }
        });
    }

    @Override
    public void stop() {
        if (handler == null) return;
        handler.post(() -> {
            if (audioTrack != null) {
                audioTrack.pause();
                audioTrack.flush();
            }
        });
    }

    @Override
    public void release() {
        released = true;
        if (handler != null) {
            handler.post(() -> {
                if (audioTrack != null) {
                    audioTrack.release();
                    audioTrack = null;
                }
                thread.quitSafely();
            });
        }
    }
}

