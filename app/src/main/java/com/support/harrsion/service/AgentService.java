package com.support.harrsion.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
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

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceUtil.createNotificationChannel(this, "agent", "Agent Service");
        Notification notification = DeviceUtil.buildNotification(this, "agent",
                "Agent Running", "AI automation in progress");
        startForeground(AppConfig.Foreground.AGENT_SERVICE_ID, notification);

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setBaseUrl(AppConfig.Model.baseUrl);
        modelConfig.setApiKey(AppConfig.Model.apiKey);
        modelConfig.setModelName(AppConfig.Model.modelName);
        agent = new Agent(getApplicationContext(), modelConfig);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String task = intent.getStringExtra("task");
        agent.run(task);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
