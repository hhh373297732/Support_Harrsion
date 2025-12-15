package com.support.harrsion.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * 唤醒服务
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class WakeUpService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
