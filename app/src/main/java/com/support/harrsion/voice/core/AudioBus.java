package com.support.harrsion.voice.core;

import android.util.Log;
import com.support.harrsion.voice.input.AudioInput;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioBus implements AudioInput.Callback {

    private static final String TAG = "AudioBus";

    // 所有的订阅者列表 (线程安全)
    private final List<AudioConsumer> consumers = new CopyOnWriteArrayList<>();

    // 持有原始的录音输入
    private final AudioInput audioInput;

    // 标记是否已经启动
    private volatile boolean isRunning = false;

    public AudioBus(AudioInput audioInput) {
        this.audioInput = audioInput;
    }

    /**
     * 启动总线：开启麦克风。
     * App启动后调用一次即可，原则上不再关闭。
     */
    public void start() {
        if (isRunning) return;
        Log.d(TAG, "AudioBus starting...");
        // 将自己作为 Callback 传给 AudioInput
        audioInput.start(this);
        isRunning = true;
    }

    /**
     * 停止总线：彻底关闭麦克风 (通常只在 App 退出时调用)
     */
    public void stop() {
        if (!isRunning) return;
        Log.d(TAG, "AudioBus stopping...");
        audioInput.stop();
        consumers.clear();
        isRunning = false;
    }

    /**
     * 注册订阅者 (相当于“插上耳机”)
     */
    public void register(AudioConsumer consumer) {
        if (!consumers.contains(consumer)) {
            consumers.add(consumer);
            Log.d(TAG, "Consumer attached: " + consumer.getClass().getSimpleName());
        }
    }

    /**
     * 退订 (相当于“拔掉耳机”)
     */
    public void unregister(AudioConsumer consumer) {
        if (consumers.remove(consumer)) {
            Log.d(TAG, "Consumer detached: " + consumer.getClass().getSimpleName());
        }
    }

    // --- AudioInput.Callback 实现 ---

    @Override
    public void onAudioFrame(byte[] data, int length) {
        // 核心逻辑：广播数据
        // 这里没有复杂的 if-else，只管分发
        if (!consumers.isEmpty()) {
            for (AudioConsumer consumer : consumers) {
                consumer.onAudioData(data, length);
            }
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e(TAG, "Microphone error", e);
        // 这里可以扩展错误分发逻辑
    }
}