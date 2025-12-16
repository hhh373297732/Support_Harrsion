package com.support.harrsion.agent.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.support.harrsion.R;
import com.support.harrsion.dto.screenshot.Screenshot;
import com.support.harrsion.service.ScreenCaptureService;

import java.util.List;

/**
 * 设备工具类
 *
 * @author harrsion
 * @date 2025/12/15
 */
public class DeviceUtil {

    public static final String ACTION_SCREENSHOT = "com.support.harrsion.ACTION_SCREENSHOT";
    private static ScreenshotCallback sCallback;

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
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

    /**
     * 触发截屏
     *
     * @param context 上下文对象
     * @param callback 截屏回调
     */
    public static void triggerScreenshot(Context context, ScreenshotCallback callback) {
        sCallback = callback;

        Intent serviceIntent = new Intent(context, ScreenCaptureService.class);
        serviceIntent.setAction(ACTION_SCREENSHOT);
        context.startForegroundService(serviceIntent);
    }


    /**
     * 处理截屏结果
     *
     * @param screenshot 截图信息
     * @param error 异常信息
     */
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

    /**
     * 将相对坐标转换为绝对坐标
     *
     * @param element 坐标数组
     * @param width 屏幕宽度
     * @param height 屏幕高度
     * @return 实际坐标数组
     */
    public static List<Float> convertRelativeToAbsolute(List<Float> element, int width, int height) {
        Float x = element.get(0) / 1000 * width;
        Float y = element.get(1) / 1000 * height;
        return List.of(x,y);
    }

    /**
     * 创建通知渠道
     *
     * @param context 上下文对象
     * @param channelId 通道ID
     * @param name 通道名称
     */
    public static void createNotificationChannel(Context context, String channelId, String name) {
        NotificationChannel channel = new NotificationChannel(channelId, name,
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 构建通知
     *
     * @param context 上下文对象
     * @param channelId 通道ID
     * @param title 通知标题
     * @param content 通知内容
     * @return 通知对象
     */
    public static Notification buildNotification(Context context, String channelId, String title, String content) {
        return new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    /**
     * 截图回调接口
     */
    public interface ScreenshotCallback {
        /**
         * 当截图数据准备好时被调用。
         * @param screenshot 包含 Base64 数据的截图对象
         */
        void onScreenshotReady(Screenshot screenshot);

        /**
         * 截图失败时被调用。
         */
        void onScreenshotFailed(String error);
    }
}
