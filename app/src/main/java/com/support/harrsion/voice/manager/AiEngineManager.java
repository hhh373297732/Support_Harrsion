package com.support.harrsion.voice.manager;

import android.content.Context;

import com.iflytek.aikit.core.AiHelper;

import java.io.File;

public final class AiEngineManager {

    private static volatile boolean initialized = false;

    public static synchronized void init(Context ctx, File resDir) {
        if (initialized) return;

        AiHelper.Params params = AiHelper.Params.builder()
                .appId("4cb2bde0")
                .apiKey("82ecfdf75ee2cb0863cf49b7a5d239aa")
                .apiSecret("OTNhZTc5MTJlNjM0NGE0MGUwZjA1YTVi")
                .workDir(resDir.getAbsolutePath())
                .ability("e867a88f2;e2e44feff")
                .build();

        AiHelper.getInst().init(ctx, params);
        initialized = true;
    }

    public static synchronized void release(String abilityId) {
        AiHelper.getInst().engineUnInit(abilityId);
    }
}
