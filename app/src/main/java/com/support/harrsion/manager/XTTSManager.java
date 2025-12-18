package com.support.harrsion.manager;

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

public class XTTSManager {
    private static final String ABILITY_ID = "e2e44feff";
    private AiHandle aiHandle;
    private TTSAudioPlayer player;
    private XTTSCallback callback;

    public interface XTTSCallback {
        void onError(int code, String msg);
        void onProgress(int pos, int len);
    }

    public XTTSManager(XTTSCallback callback) {
        this.callback = callback;
        this.player = new TTSAudioPlayer();
        player.init();
        AiHelper.getInst().registerListener(ABILITY_ID, aiListener); // 注册监听
    }

    private final AiListener aiListener = new AiListener() {
        @Override
        public void onResult(int handleID, List<AiResponse> list, Object usrContext) {
            for (AiResponse res : list) {
                if ("audio".equals(res.getKey())) {
                    player.write(res.getValue()); // 将合成音频喂给播放器
                }
            }
        }

        @Override
        public void onEvent(int handleID, int event, List<AiResponse> eventData, Object usrContext) {
            if (event == AeeEvent.AEE_EVENT_END.getValue()) {
                AiHelper.getInst().end(aiHandle); // 引擎计算结束
            }
        }

        @Override
        public void onError(int handleID, int err, String msg, Object usrContext) {
            if (callback != null) callback.onError(err, msg);
        }
    };

    public void startSpeaking(String text, XTTSParams params) {
        player.stop(); // 开始新合成前停止旧播放

        AiRequest input = AiInput.builder()
                .param("vcn", params.getVcn())
                .param("language", params.getLanguage())
                .param("pitch", params.getPitch())
                .param("volume", params.getVolume())
                .param("speed", params.getSpeed())
                .param("textEncoding", "UTF-8")
                .build();

        aiHandle = AiHelper.getInst().start(ABILITY_ID, input, null); // 启动能力

        if (aiHandle.getCode() == 0) {
            AiRequest request = AiRequest.builder()
                    .payload(AiText.get("text").data(text).valid())
                    .build();
            AiHelper.getInst().write(request, aiHandle); // 写入文本
        }
    }

    public void release() {
        player.release();
        AiHelper.getInst().engineUnInit(ABILITY_ID); // 逆初始化
    }
}