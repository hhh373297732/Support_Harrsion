package com.support.harrsion.voice.core;

import android.util.Log;

/**
 * 语音内核 (The Brain)：负责协调听和说，管理系统状态。
 */
public class VoiceKernel {

    private static final String TAG = "VoiceKernel";

    // 定义系统状态
    public enum State {
        IDLE,       // 待机：在听唤醒词
        SPEAKING    // 说话：正在播报（此时不听，防误触）
    }

    private final AudioBus audioBus;
    private final WakeUpWorker wakeUpWorker;
    private final TtsWorker ttsWorker;

    private State currentState = State.IDLE;

    public VoiceKernel(AudioBus audioBus, WakeUpWorker wakeUpWorker, TtsWorker ttsWorker) {
        this.audioBus = audioBus;
        this.wakeUpWorker = wakeUpWorker;
        this.ttsWorker = ttsWorker;

        initCallbacks();
    }

    /**
     * 初始化回调：连接 耳朵 和 嘴巴 到 大脑
     */
    private void initCallbacks() {
        // 1. 当唤醒成功时
        wakeUpWorker.setCallback(word -> {
            Log.i(TAG, ">>> 唤醒成功: " + word);
            // 收到唤醒 -> 变身 -> 说话
            // 注意：这里可以根据 word 内容决定说什么
            ttsWorker.speak("在呢，老板有什么吩咐？");
        });

        // 2. 当 TTS 状态变化时
        ttsWorker.setCallback(new TtsWorker.Callback() {
            @Override
            public void onTtsStart() {
                // 开始说话了 -> 切换状态到 SPEAKING
                switchState(State.SPEAKING);
            }

            @Override
            public void onTtsComplete() {
                // 说完了 -> 切换状态回 IDLE
                switchState(State.IDLE);
            }
        });
    }

    /**
     * 启动整个系统
     */
    public void start() {
        Log.d(TAG, "Kernel System Starting...");
        audioBus.start(); // 1. 开启总线（麦克风常驻）
        wakeUpWorker.start(); // 2. 让唤醒引擎准备好

        // 3. 初始状态设为 IDLE
        switchState(State.IDLE);
    }

    /**
     * 停止整个系统 (App退出时)
     */
    public void stop() {
        wakeUpWorker.stop();
        ttsWorker.stop();
        audioBus.stop();
    }

    /**
     * 核心状态机：根据状态决定由于谁来监听音频
     */
    private synchronized void switchState(State newState) {
        if (currentState == newState) return;
        Log.d(TAG, "State Change: " + currentState + " -> " + newState);

        currentState = newState;

        switch (newState) {
            case IDLE:
                // 待机模式：把唤醒工人注册到总线上
                // 此时：麦克风数据 -> AudioBus -> WakeUpWorker
                audioBus.register(wakeUpWorker);
                break;

            case SPEAKING:
                // 说话模式：把唤醒工人踢掉
                // 此时：麦克风数据 -> AudioBus -> (Nobody)
                // 这样就物理屏蔽了“自言自语”导致的误唤醒
                audioBus.unregister(wakeUpWorker);
                break;
        }
    }
}