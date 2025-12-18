package com.support.harrsion.manager;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.iflytek.aikit.core.AiAudio;
import com.iflytek.aikit.core.AiHandle;
import com.iflytek.aikit.core.AiHelper;
import com.iflytek.aikit.core.AiListener;
import com.iflytek.aikit.core.AiRequest;
import com.iflytek.aikit.core.AiResponse;
import com.iflytek.aikit.core.AiStatus;

import java.util.List;

public class IVWManager {
    private static final String ABILITY_ID = "e867a88f2";
    private AiHandle aiHandle;
    private AudioRecord audioRecord;
    private boolean isWorking = false;

    // SDK 回调接口
    public interface IVWCallback {
        void onWakeup(String result);
        void onError(String error);
    }

    /**
     * 初始化并开始唤醒监听
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void start(String keywordPath, IVWCallback callback) {
        if (isWorking) return;

        // 1. 加载资源并设置唤醒词
        AiRequest loadReq = AiRequest.builder().customText("key_word", keywordPath, 0).build();
        if (AiHelper.getInst().loadData(ABILITY_ID, loadReq) != 0) return;

        AiHelper.getInst().specifyDataSet(ABILITY_ID, "key_word", new int[]{0});

        // 2. 启动引擎
        AiRequest startReq = AiRequest.builder()
                .param("wdec_param_nCmThreshold", "0 0:800")
                .param("gramLoad", true).build();

        aiHandle = AiHelper.getInst().start(ABILITY_ID, startReq, null);

        // 3. 注册监听
        AiHelper.getInst().registerListener(ABILITY_ID, new AiListener() {
            @Override
            public void onResult(int id, List<AiResponse> outputs, Object ctx) {
                for (AiResponse resp : outputs) {
                    if (resp.getKey().equals("func_wake_up")) {
                        callback.onWakeup(new String(resp.getValue()));
                    }
                }
            }
            @Override public void onError(int i, int i1, String s, Object o) { callback.onError(s); }
            @Override public void onEvent(int i, int i1, List<AiResponse> list, Object o) {}
        });

        startRecording();
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private void startRecording() {
        isWorking = true;
        int bufferSize = 1280;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        audioRecord.startRecording();

        new Thread(() -> {
            byte[] buffer = new byte[bufferSize];
            while (isWorking) {
                int read = audioRecord.read(buffer, 0, bufferSize);
                if (read > 0) {
                    AiAudio audio = AiAudio.get("wav").data(buffer).status(AiStatus.CONTINUE).valid();
                    AiHelper.getInst().write(AiRequest.builder().payload(audio).build(), aiHandle);
                }
            }
        }).start();
    }

    public void stop() {
        isWorking = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
        }
        if (aiHandle != null) {
            AiHelper.getInst().end(aiHandle);
        }
    }
}
