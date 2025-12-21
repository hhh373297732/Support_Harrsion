package com.support.harrsion.voice.engine;

import com.iflytek.aikit.core.AiAudio;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiListener;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiStatus;
import com.support.harrsion.voice.VoiceState;

import java.util.Arrays;
import java.util.List;

public class IflytekIvwEngine implements IvwEngine {

    private static final String TAG = "WakeUpEngine";
    private static final String ABILITY_ID = "e867a88f2";

    private final String keywordPath;
    private Callback callback;

    private AiHandle aiHandle;
    private boolean started = false;

    public IflytekIvwEngine(String keywordPath) {
        this.keywordPath = keywordPath;
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void start() {
        // 1. 加载唤醒词
        AiRequest loadReq = AiRequest.builder()
                .customText("key_word", keywordPath, 0)
                .build();

        int ret = AiHelper.getInst().loadData(ABILITY_ID, loadReq);
        if (ret != 0) {
            callback.onError("load keyword failed: " + ret);
            return;
        }

        AiHelper.getInst().specifyDataSet(
                ABILITY_ID,
                "key_word",
                new int[]{0}
        );

        // 2. 启动引擎
        AiRequest startReq = AiRequest.builder()
                .param("wdec_param_nCmThreshold", "0 0:800")
                .param("gramLoad", true)
                .build();

        aiHandle = AiHelper.getInst().start(
                ABILITY_ID,
                startReq,
                null
        );

        if (aiHandle == null || aiHandle.getCode() != 0) {
            callback.onError("engine start failed");
            return;
        }

        started = true;

        // 3. 注册监听
        AiHelper.getInst().registerListener(ABILITY_ID, aiListener);
    }

    @Override
    public void onVoice(VoiceState state, byte[] audio, int length) {
        if (!started || aiHandle == null) return;

        AiStatus status;
        switch (state) {
            case VOICE_START:
                status = AiStatus.BEGIN;
                break;
            case VOICE_ACTIVE:
                status = AiStatus.CONTINUE;
                break;
            case VOICE_END:
                status = AiStatus.END;
                break;
            default:
                return;
        }

        byte[] data = (length == audio.length)
                ? audio
                : Arrays.copyOf(audio, length);

        AiAudio aiAudio = AiAudio.get("wav")
                .data(data)
                .status(status)
                .valid();

        AiHelper.getInst().write(
                AiRequest.builder().payload(aiAudio).build(),
                aiHandle
        );
    }

    @Override
    public void stop() {
        started = false;
        if (aiHandle != null) {
            AiHelper.getInst().end(aiHandle);
            aiHandle = null;
        }
    }

    private final AiListener aiListener = new AiListener() {

        @Override
        public void onResult(int id, List<AiResponse> outputs, Object ctx) {
            if (outputs == null) return;

            for (AiResponse resp : outputs) {
                if ("func_wake_up".equals(resp.getKey())) {
                    byte[] val = resp.getValue();
                    if (val != null) {
                        callback.onWakeUp(new String(val));
                    }
                }
            }
        }

        @Override
        public void onError(int id, int code, String msg, Object ctx) {
            callback.onError(msg);
        }

        @Override
        public void onEvent(int i, int i1, List<AiResponse> list, Object o) {}
    };
}
