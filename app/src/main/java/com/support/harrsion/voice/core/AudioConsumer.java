package com.support.harrsion.voice.core;

public interface AudioConsumer {
    /**
     * 当麦克风采集到音频数据时回调
     * @param data PCM原始数据
     * @param length 有效数据长度
     */
    void onAudioData(byte[] data, int length);
}