package com.support.harrsion.voice.output;

public interface AudioOutput {

    void start();
    void write(byte[] pcm);
    void stop();
    void release();
}
