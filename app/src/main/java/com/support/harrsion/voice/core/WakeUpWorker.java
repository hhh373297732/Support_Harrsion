package com.support.harrsion.voice.core;

import android.util.Log;
import com.support.harrsion.voice.VoiceState;
import com.support.harrsion.voice.engine.IvwEngine;
import com.support.harrsion.voice.vad.VoiceActivityDetector;

import lombok.Setter;

/**
 * 唤醒工人：负责监听音频流，进行VAD检测，并驱动唤醒引擎。
 * 它实现了 AudioConsumer，所以可以直接插在 AudioBus 上。
 */
public class WakeUpWorker implements AudioConsumer, VoiceActivityDetector.Callback {

    private static final String TAG = "WakeUpWorker";

    private final VoiceActivityDetector vad;
    private final IvwEngine engine;
    @Setter
    private Callback callback; // 用于通知 Kernel

    public interface Callback {
        void onWakeUpSuccess(String word);
    }

    public WakeUpWorker(VoiceActivityDetector vad, IvwEngine engine) {
        this.vad = vad;
        this.engine = engine;
        initInternalCallbacks();
    }

    private void initInternalCallbacks() {
        // 1. 设置 VAD 回调：VAD 也就是 this (见下文 onVoiceState)
        this.vad.setCallback(this);

        // 2. 设置引擎回调：引擎成功了，通知我的 Callback
        this.engine.setCallback(new IvwEngine.Callback() {
            @Override
            public void onWakeUp(String result) {
                Log.d(TAG, "WakeUp Detected: " + result);
                if (callback != null) {
                    callback.onWakeUpSuccess(result);
                }
            }

            @Override
            public void onError(String msg) {
                Log.e(TAG, "Engine Error: " + msg);
            }
        });
    }

    // --- AudioConsumer 实现 (从总线拿数据) ---
    @Override
    public void onAudioData(byte[] data, int length) {
        // 拿到原始数据，先给 VAD 过滤
        vad.onAudioFrame(data, length);
    }

    // --- VoiceActivityDetector.Callback 实现 (VAD 通知有动静) ---
    @Override
    public void onVoiceState(VoiceState state, byte[] audio, int length) {
        // VAD 说有人说话（或者结束说话），喂给唤醒引擎
        engine.onVoice(state, audio, length);
    }

    // --- 生命周期控制 ---

    // 只有“上班”的时候才启动引擎
    public void start() {
        engine.start();
        vad.reset(); // 每次开始前重置 VAD 状态
    }

    // “下班”的时候关闭引擎
    public void stop() {
        engine.stop();
        vad.reset();
    }
}