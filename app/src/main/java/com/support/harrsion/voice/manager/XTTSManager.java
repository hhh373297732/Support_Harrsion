package com.support.harrsion.voice.manager;

import com.support.harrsion.dto.xtts.XTTSParams;
import com.support.harrsion.voice.output.AudioTrackOutput;
import com.support.harrsion.voice.engine.IflytekTtsEngine;
import com.support.harrsion.voice.output.AudioOutput;
import com.support.harrsion.voice.engine.TtsEngine;
import com.support.harrsion.voice.pipeline.VoicePipelineImpl;

import lombok.Setter;

public class XTTSManager implements TtsEngine.Callback {

    private final TtsEngine engine;
    private final AudioOutput output;
    @Setter
    private VoicePipelineImpl pipeline;
    @Setter
    private TtsCompletionListener completionListener;

    // 在 XTTSManager 类内部添加
    public interface TtsCompletionListener {
        void onTtsFinished();
    }

    public XTTSManager() {
        output = new AudioTrackOutput();
        engine = new IflytekTtsEngine(this);
        output.start();
    }

    public void speak(String text, XTTSParams params) {
        engine.start(text, params);
    }

    public void stop() {
        engine.stop();
        output.stop();
    }

    @Override
    public void onAudio(byte[] pcm) {
        output.write(pcm);
    }

    @Override
    public void onComplete() {
        if (completionListener != null) {
            completionListener.onTtsFinished();
        }
    }

    @Override
    public void onError(int code, String msg) {
        output.stop(); // 出错时可以停
        if (completionListener != null) {
            completionListener.onTtsFinished(); // 出错也视为结束，以便恢复唤醒
        }
    }
}
