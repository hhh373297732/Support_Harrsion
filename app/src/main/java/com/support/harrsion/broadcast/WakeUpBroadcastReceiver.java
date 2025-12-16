package com.support.harrsion.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.support.harrsion.service.WakeUpService;

/**
 * 唤醒服务广播接收器
 *
 * @author harrsion
 * @date 2025/12/16
 */
public class WakeUpBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_WAKE_UP = "com.support.harrsion.action.PorcupineInitError";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WakeUpBroadcastReceiver", "收到广播，action: " + intent.getAction());
        if (ACTION_WAKE_UP.equals(intent.getAction())) {
            initError(context, intent.getStringExtra("errorMessage"));
        }
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, WakeUpService.class);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    public static void stopService(Context context) {
        Intent serviceIntent = new Intent(context, WakeUpService.class);
        context.stopService(serviceIntent);
    }

    public static void initError(Context context, final String errorMessage) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show());
        stopService(context);
    }
}
