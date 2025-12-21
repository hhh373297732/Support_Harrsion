package com.support.harrsion.voice.pipeline;

import android.util.Log;

import com.support.harrsion.dto.xtts.XTTSParams;
import com.support.harrsion.voice.flow.TtsFlow;
import com.support.harrsion.voice.flow.IvwFlow;

public class VoicePipelineImpl implements VoicePipeline {

    private final IvwFlow ivwFlow;
    private final TtsFlow ttsFlow;

    /** 策略配置 */
    private final boolean pauseWakeUpWhenSpeaking;
    private final boolean interruptTtsOnWakeUp;
    private final XTTSParams xttsParams = new XTTSParams();

    public VoicePipelineImpl(
            IvwFlow ivwFlow,
            TtsFlow ttsFlow,
            boolean pauseWakeUpWhenSpeaking,
            boolean interruptTtsOnWakeUp
    ) {
        this.ivwFlow = ivwFlow;
        this.ttsFlow = ttsFlow;
        this.pauseWakeUpWhenSpeaking = pauseWakeUpWhenSpeaking;
        this.interruptTtsOnWakeUp = interruptTtsOnWakeUp;
    }

    @Override
    public void start() {
        ivwFlow.start();
    }

    @Override
    public void stop() {
        ivwFlow.stop();
        ttsFlow.stop();
    }

    @Override
    public void speak(String text, XTTSParams params) {
        if (pauseWakeUpWhenSpeaking) {
            ivwFlow.stop();
        }
        ttsFlow.speak(text, params);
    }

    /** 唤醒回调入口（由 WakeUpEngine 触发） */
    public void onWakeUp(String result) {
        Log.d("VoicePipeline", "onWakeUp: " + result);
        if (interruptTtsOnWakeUp) {
            ttsFlow.stop();
        }

        speak("在，老板有什么吩咐？", xttsParams);
    }

    /** TTS 完成回调 */
    public void onTtsComplete() {
        if (pauseWakeUpWhenSpeaking) {
            ivwFlow.start();
        }
    }
}
