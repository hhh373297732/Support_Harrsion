package com.support.harrsion.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

/**
 * 权限工具类
 *
 * @author harrsion
 * @date 2025/12/16
 */
public class PermissionUtil {

    /**
     * 检测录音权限
     */
    public static boolean hasRecordPermission(Context context) {
        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检测通知权限
     */
    public static boolean hasNotificationPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求权限
     * @param activity
     * @param permissions 权限列表
     */
    public static  void requestPermissions(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions, 0);
    }
}
