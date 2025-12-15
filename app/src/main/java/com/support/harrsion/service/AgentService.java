package com.support.harrsion.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.support.harrsion.R;
import com.support.harrsion.agent.Agent;
import com.support.harrsion.agent.utils.DeviceUtil;
import com.support.harrsion.dto.model.ModelConfig;

/**
 * Agent服务
 *
 * @describe 挂在后台，避免跳转到第三方app的时候任务无法继续运行
 * @author harrsion
 * @date 2025/12/15
 */
public class AgentService extends Service {

    private static final int AGENT_ID = 110;
    private Agent agent;

    @Override
    public void onCreate() {
        super.onCreate();
        DeviceUtil.createNotificationChannel(this, "agent", "Agent Service");
        Notification notification = DeviceUtil.buildNotification(this, "agent",
                "Agent Running", "AI automation in progress");
        startForeground(AGENT_ID, notification);

        ModelConfig modelConfig = new ModelConfig();
        modelConfig.setBaseUrl("https://open.bigmodel.cn/api/paas/v4");
        modelConfig.setApiKey("af8c20abc40d466ab939c07ca7359912.ZnyCoeosnZYZGQTJ");
        modelConfig.setModelName("autoglm-phone");
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
