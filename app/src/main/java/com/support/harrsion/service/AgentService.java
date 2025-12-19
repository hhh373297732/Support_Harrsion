package com.support.harrsion.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.support.harrsion.agent.Agent;
import com.support.harrsion.utils.DeviceUtil;
import com.support.harrsion.config.AppConfig;
import com.support.harrsion.dto.model.ModelConfig;

/**
 * Agent服务
 *
 * @describe 挂在后台，避免跳转到第三方app的时候任务无法继续运行
 * @author harrsion
 * @date 2025/12/15
 */
public class AgentService extends Service {
    private Agent agent;

    // SharedPreferences keys (mirroring MainActivity)
    private static final String PREFS_NAME = "ModelSettings";
    private static final String PREF_MODEL_NAME = "modelName";
    private static final String PREF_API_KEY = "apiKey";
    private static final String PREF_SELECTED_MODEL_INDEX = "selectedModelIndex";

    // Model options and corresponding URLs (mirroring MainActivity)
    private static final String[][] MODEL_OPTIONS = {
        {"智谱 (autoglm-phone)", "https://open.bigmodel.cn/api/paas/v4", "autoglm-phone"},
        {"OpenAI (gpt-4)", "https://api.openai.com/v1", "gpt-4"},
        {"自定义", "", "custom-model"}
    };

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceUtil.createNotificationChannel(this, "agent", "Agent Service");
        Notification notification = DeviceUtil.buildNotification(this, "agent",
                "Agent Running", "AI automation in progress");
        startForeground(AppConfig.Foreground.AGENT_SERVICE_ID, notification);

        // Load user settings from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize ModelConfig with user settings
        ModelConfig modelConfig = new ModelConfig();

        // Get selected model index and determine baseUrl
        int selectedModelIndex = sharedPreferences.getInt(PREF_SELECTED_MODEL_INDEX, 0);
        String baseUrl;
        if (selectedModelIndex == 2) { // 自定义模型，目前保持默认
            baseUrl = "";
        } else {
            baseUrl = MODEL_OPTIONS[selectedModelIndex][1];
        }
        modelConfig.setBaseUrl(baseUrl);

        // Get apiKey from settings
        String apiKey = sharedPreferences.getString(PREF_API_KEY, "");
        modelConfig.setApiKey(apiKey);

        // Get modelName from settings or use default
        String modelName = sharedPreferences.getString(PREF_MODEL_NAME, "autoglm-phone");
        modelConfig.setModelName(modelName);

        agent = new Agent(getApplicationContext(), modelConfig);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String task = intent.getStringExtra("task");
            agent.run(task);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}