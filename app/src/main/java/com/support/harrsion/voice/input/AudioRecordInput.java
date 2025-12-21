package com.support.harrsion.voice.input;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

public class AudioRecordInput implements AudioInput {

    private static final int SAMPLE_RATE = 16000;

    private AudioRecord audioRecord;
    private Thread recordThread;
    private volatile boolean running = false;

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @Override
    public void start(Callback callback) {
        if (running) return;
        running = true;

        recordThread = new Thread(() -> {
            int bufferSize = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
            );
            if (bufferSize <= 0) bufferSize = 1280;

            try {
                audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );

                audioRecord.startRecording();
                byte[] buffer = new byte[bufferSize];

                while (running) {
                    int read = audioRecord.read(buffer, 0, bufferSize);
                    if (read > 0) {
                        callback.onAudioFrame(buffer, read);
                    }
                }
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                release();
            }
        }, "AudioRecordInput");

        recordThread.start();
    }

    @Override
    public void stop() {
        running = false;
        if (recordThread != null) {
            recordThread.interrupt();
            recordThread = null;
        }
        release();
    }

    private void release() {
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (Exception ignored) {}
            audioRecord.release();
            audioRecord = null;
        }
    }
}
