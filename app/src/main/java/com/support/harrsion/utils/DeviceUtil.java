package com.support.harrsion.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.support.harrsion.MainActivity;
import com.support.harrsion.R;
import com.support.harrsion.dto.screenshot.Screenshot;
import com.support.harrsion.service.ScreenCaptureService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private static final String TAG = "DeviceUtil";

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
     * @param context  上下文对象
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
     * @param error      异常信息
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
     * @param width   屏幕宽度
     * @param height  屏幕高度
     * @return 实际坐标数组
     */
    public static List<Float> convertRelativeToAbsolute(List<Float> element, int width, int height) {
        Float x = element.get(0) / 1000 * width;
        Float y = element.get(1) / 1000 * height;
        return List.of(x, y);
    }

    /**
     * 创建通知渠道
     *
     * @param context   上下文对象
     * @param channelId 通道ID
     * @param name      通道名称
     */
    public static void createNotificationChannel(Context context, String channelId, String name) {
        NotificationChannel channel = new NotificationChannel(channelId, name,
                NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 创建通知渠道
     *
     * @param context   上下文对象
     * @param channelId 通道ID
     * @param name      通道名称
     * @param importance 通道重要程度
     */
    public static void createNotificationChannel(Context context, String channelId, String name, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, name, importance);
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    /**
     * 构建通知
     *
     * @param context   上下文对象
     * @param channelId 通道ID
     * @param title     通知标题
     * @param content   通知内容
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
     * 将 assets 里的文件复制到应用的私有缓存目录，并返回该文件的绝对路径。
     * * @param context Context
     *
     * @param assetFileName  assets 里的文件名，如 "data/model.dat"
     * @param targetFileName 复制后的文件名，如 "copied_model.dat"
     * @return 复制后文件的绝对路径，如果失败返回 null
     */
    public static String copyAssetFileToCache(Context context, String assetFileName, String targetFileName) {
        // 1. 定义目标文件路径
        File cacheDir = context.getCacheDir();
        File targetFile = new File(cacheDir, targetFileName);

        // 2. 检查文件是否已存在
        if (targetFile.exists() && targetFile.length() > 0) {
            // 文件已存在且大小大于 0，直接返回路径
            android.util.Log.d(TAG, targetFileName + " 文件已存在于缓存目录，直接使用。");
            return targetFile.getAbsolutePath();
        }

        // 3. 文件不存在或为空，执行复制操作
        android.util.Log.d(TAG, targetFileName + " 文件不存在或为空，开始从 assets 复制。");

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open(assetFileName);
            outputStream = new FileOutputStream(targetFile);

            // 复制文件内容
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

            android.util.Log.d(TAG, targetFileName + " 复制成功，路径: " + targetFile.getAbsolutePath());
            return targetFile.getAbsolutePath(); // 复制成功，返回路径

        } catch (IOException e) {
            android.util.Log.e(TAG, "从 assets 复制文件失败: " + assetFileName, e);

            // 如果复制失败，尝试删除可能创建的不完整文件
            if (targetFile.exists()) {
                targetFile.delete();
            }
            return null; // 复制失败，返回 null
        } finally {
            // 确保流被关闭
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException ignored) {
            }
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * 截图回调接口
     */
    public interface ScreenshotCallback {
        /**
         * 当截图数据准备好时被调用。
         *
         * @param screenshot 包含 Base64 数据的截图对象
         */
        void onScreenshotReady(Screenshot screenshot);

        /**
         * 截图失败时被调用。
         */
        void onScreenshotFailed(String error);
    }
}
