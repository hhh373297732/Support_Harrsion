package com.support.harrsion.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.support.harrsion.MainActivity;
import com.support.harrsion.R;
import com.support.harrsion.agent.utils.DeviceUtil;

import ai.picovoice.porcupine.Porcupine;
import ai.picovoice.porcupine.PorcupineActivationException;
import ai.picovoice.porcupine.PorcupineActivationLimitException;
import ai.picovoice.porcupine.PorcupineActivationRefusedException;
import ai.picovoice.porcupine.PorcupineActivationThrottledException;
import ai.picovoice.porcupine.PorcupineException;
import ai.picovoice.porcupine.PorcupineInvalidArgumentException;
import ai.picovoice.porcupine.PorcupineManager;
import ai.picovoice.porcupine.PorcupineManagerCallback;

/**
 * 唤醒服务
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class WakeUpService extends Service {

    private static final String CHANNEL_ID = "PorcupineServiceChannel";
    private static final String ACCESS_KEY = "ZvRw/ShkkCoysqN06aQfBkXVOUaTMFqp3v3us/Pk999OML4TSYAE0g==";

    private PorcupineManager porcupineManager;
    private int numUtterances;
    private final PorcupineManagerCallback porcupineManagerCallback = (keywordIndex) -> {
        numUtterances++;

        final String contentText = numUtterances == 1 ? "!" : (" ，第" + numUtterances + "次了哦!");
        Notification n = getNotification(
                "Wake word",
                "小陈听到了" + contentText);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(1234, n);
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        numUtterances = 0;
        DeviceUtil.createNotificationChannel(this, CHANNEL_ID, "Porcupine",
                NotificationManager.IMPORTANCE_HIGH);

        try {
            porcupineManager = new PorcupineManager.Builder()
                    .setAccessKey(ACCESS_KEY)
                    .setKeywordPath(DeviceUtil.copyAssetFileToCache(this,
                            "小陈_zh_android_v4_0_0.ppn", "cache_小陈_zh_android_v4_0_0.ppn"))
                    .setModelPath(DeviceUtil.copyAssetFileToCache(this,
                            "porcupine_params_zh.pv", "cache_porcupine_params_zh.pv"))
                    .setSensitivity(0.7f).build(
                            getApplicationContext(),
                            porcupineManagerCallback);
            porcupineManager.start();

        } catch (PorcupineInvalidArgumentException e) {
            onPorcupineInitError(e.getMessage());
        } catch (PorcupineActivationException e) {
            onPorcupineInitError("AccessKey activation error");
        } catch (PorcupineActivationLimitException e) {
            onPorcupineInitError("AccessKey reached its device limit");
        } catch (PorcupineActivationRefusedException e) {
            onPorcupineInitError("AccessKey refused");
        } catch (PorcupineActivationThrottledException e) {
            onPorcupineInitError("AccessKey has been throttled");
        } catch (PorcupineException e) {
            onPorcupineInitError("Failed to initialize Porcupine: " + e.getMessage());
        }

        Notification notification = porcupineManager == null ?
                getNotification("Porcupine init failed", "Service will be shut down") :
                getNotification("Wake word service", "Say '小陈'!");
        startForeground(1234, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    private void onPorcupineInitError(String message) {
        Intent i = new Intent("PorcupineInitError");
        Log.e("PORCUPINE", "porcupine init error: " + message);
        i.putExtra("errorMessage", message);
        sendBroadcast(i);
    }

    private Notification getNotification(String title, String message) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (porcupineManager != null) {
            try {
                porcupineManager.stop();
                porcupineManager.delete();
            } catch (PorcupineException e) {
                Log.e("PORCUPINE", e.toString());
            }
        }

        super.onDestroy();
    }
}
