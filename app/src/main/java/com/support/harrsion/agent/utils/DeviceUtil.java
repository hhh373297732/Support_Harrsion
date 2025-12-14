package com.support.harrsion.agent.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.support.harrsion.agent.device.ScreenshotCallback;
import com.support.harrsion.dto.screenshot.Screenshot;
import com.support.harrsion.service.ScreenCaptureService;

import java.util.List;

public class DeviceUtil {

    public static final String ACTION_SCREENSHOT = "com.support.harrsion.ACTION_SCREENSHOT";
    private static ScreenshotCallback sCallback;
    public static String getHardwareDeviceName() {
        // 制造商名称 (e.g., Samsung, Google)
        String manufacturer = Build.MANUFACTURER;

        // 型号名称 (e.g., SM-G998U, Pixel 6)
        String model = Build.MODEL;

        // 确保型号名称不包含制造商名称，避免冗余
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public static void triggerScreenshot(Context context, ScreenshotCallback callback) {
        sCallback = callback; // 存储回调

        Intent serviceIntent = new Intent(context, ScreenCaptureService.class);
        serviceIntent.setAction(ACTION_SCREENSHOT);
        context.startForegroundService(serviceIntent);
    }

    // 🌟 新增：由 LocalBroadcastReceiver 调用此方法
    public static void handleScreenshotResult(Screenshot screenshot, String error) {
        if (sCallback != null) {
            if (error != null) {
                sCallback.onScreenshotFailed(error);
            } else {
                sCallback.onScreenshotReady(screenshot);
            }
            sCallback = null; // 处理完后清除回调
        }
    }

    // 计算实际坐标
    public static List<Float> convertRelativeToAbsolute(List<Float> element, int width, int height) {
        Float x = element.get(0) / 1000 * width;
        Float y = element.get(1) / 1000 * height;
        return List.of(x,y);
    }
}
