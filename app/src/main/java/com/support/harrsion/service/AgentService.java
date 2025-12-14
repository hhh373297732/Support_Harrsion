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
import com.support.harrsion.dto.model.ModelConfig;

public class AgentService extends Service {

    private static final int AGENT_ID = 110;
    private Agent agent;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(AGENT_ID, buildNotification());

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

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                "agent",
                "Agent Service",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "agent")
                .setContentTitle("Agent Running")
                .setContentText("AI automation in progress")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}
