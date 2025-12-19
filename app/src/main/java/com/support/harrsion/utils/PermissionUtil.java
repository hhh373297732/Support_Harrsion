package com.support.harrsion.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.support.harrsion.service.ActionService;
import com.support.harrsion.service.ScreenCaptureService;

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
     *
     * @param activity    上下文对象
     * @param permissions 权限列表
     */
    public static void requestPermissions(ComponentActivity activity, String[] permissions) {

        ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions()
                , result -> {
                    for (String permission : permissions) {
                        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
                            DeviceUtil.startWakeUpService(activity, false);
                        }
                    }
                    Toast.makeText(activity, "权限已允许", Toast.LENGTH_SHORT).show();
                });
        requestMultiplePermissionsLauncher.launch(permissions);
    }

    /**
     * 打开无障碍服务设置页面
     *
     * @param context 上下文对象
     */
    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 判断无障碍服务是否已启用
     *
     * @param context 上下文对象
     */
    public static boolean isAccessibilityServiceEnabled(Context context) {
        String service = context.getPackageName() + "/" +
                ActionService.class.getCanonicalName();

        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        return enabledServices != null && enabledServices.contains(service);
    }


    /**
     * 启动系统 Intent，请求屏幕捕获权限。
     */
    public static void requestScreenCapturePermission(ComponentActivity activity) {
        MediaProjectionManager mProjectionManager =
                (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mProjectionManager != null) {
            ActivityResultLauncher<Intent> screenCaptureLauncher = activity.registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        int resultCode = result.getResultCode();
                        Intent data = result.getData();
                        if (resultCode == Activity.RESULT_OK) {
                            // 用户授权成功
                            Toast.makeText(activity, "屏幕捕获已授权，正在启动服务...", Toast.LENGTH_SHORT).show();
                            // 启动后台服务，并将授权结果传递给它
                            Intent serviceIntent = new Intent(activity, ScreenCaptureService.class);
                            serviceIntent.putExtra("resultCode", resultCode);
                            serviceIntent.putExtra("data", data);
                            activity.startForegroundService(serviceIntent);
                        } else {
                            // 用户拒绝授权
                            Toast.makeText(activity, "用户拒绝屏幕捕获", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            screenCaptureLauncher.launch(mProjectionManager.createScreenCaptureIntent());
        } else {
            Toast.makeText(activity, "系统不支持屏幕捕获", Toast.LENGTH_SHORT).show();
        }
    }
}
