package com.support.harrsion.voice.engine;

import android.util.Log;

import com.iflytek.aikit.core.AeeEvent;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiInput;
import com.iflytek.aikit.core.AiListener;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiText;
import com.support.harrsion.dto.xtts.XTTSParams;

import java.util.List;

public class IflytekTtsEngine implements TtsEngine {

    private static final String ABILITY_ID = "e2e44feff";

    private final Callback callback;
    private AiHandle handle;

    public IflytekTtsEngine(Callback callback) {
        this.callback = callback;
        AiHelper.getInst().registerListener(ABILITY_ID, listener);
    }

    @Override
    public void start(String text, XTTSParams params) {
        AiRequest input = AiInput.builder()
                .param("vcn", params.getVcn())
                .param("language", params.getLanguage())
                .param("pitch", params.getPitch())
                .param("speed", params.getSpeed())
                .param("volume", params.getVolume())
                .param("textEncoding", params.getTextEncoding())
                .build();

        handle = AiHelper.getInst().start(ABILITY_ID, input, null);
        if (handle == null || handle.getCode() != 0) {
            callback.onError(-1, "engine start failed");
            return;
        }

        AiHelper.getInst().write(
                AiRequest.builder()
                        .payload(AiText.get("text").data(text).valid())
                        .build(),
                handle
        );
    }

    @Override
    public void stop() {
        Log.d("IflytekTtsEngine", "stop");
        if (handle != null) {
            AiHelper.getInst().end(handle);
            handle = null;
        }
    }

    private final AiListener listener = new AiListener() {
        @Override
        public void onResult(int id, List<AiResponse> list, Object ctx) {
            Log.d("IflytekTtsEngine", "onResult");
            for (AiResponse res : list) {
                if ("audio".equals(res.getKey())) {
                    callback.onAudio(res.getValue());
                }
            }
        }

        @Override
        public void onEvent(int id, int event, List<AiResponse> data, Object ctx) {
            Log.d("IflytekTtsEngine", "onEvent");
            if (event == AeeEvent.AEE_EVENT_END.getValue()) {
                callback.onComplete();
                stop();
            }
        }

        @Override
        public void onError(int id, int err, String msg, Object ctx) {
            callback.onError(err, msg);
        }
    };
}
