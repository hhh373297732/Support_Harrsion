package com.support.harrsion;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.support.harrsion.service.AgentService;
import com.support.harrsion.service.ActionService;
import com.support.harrsion.service.ScreenCaptureService;
import com.support.harrsion.service.WakeUpService;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1001;
    private static final int REQUEST_CODE_AUDIO = 1002;
    private MediaProjectionManager mProjectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 假设您的布局文件中有一个ID为 btn_start_capture 的按钮
        setContentView(R.layout.activity_main);

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        requestScreenCapturePermission();

        ArrayList<String> permissionsToRequest  = new ArrayList<>();
        if (!hasNotificationPermission()) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!hasRecordPermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissionsToRequest.isEmpty()) {
            startService();
        } else {
            requestRecordPermissions(permissionsToRequest.toArray(new String[0]));
        }

        EditText taskInput = findViewById(R.id.task_content);

        Button takeScreenshotButton = findViewById(R.id.btn_start_task);
        takeScreenshotButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AgentService.class);
            intent.putExtra("task", taskInput.getText().toString());
            startForegroundService(intent);
        });

        if (!isAccessibilityServiceEnabled(this)) {
            openAccessibilitySettings(this);
        }
    }

    public static void openAccessibilitySettings(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isAccessibilityServiceEnabled(Context context) {
        String service = context.getPackageName() + "/" +
                ActionService.class.getCanonicalName();

        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        return enabledServices != null && enabledServices.contains(service);
    }

    private boolean hasRecordPermission() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasNotificationPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(
                this,
                permissions,
                0);
    }

    /**
     * 启动系统 Intent，请求屏幕捕获权限。
     */
    private void requestScreenCapturePermission() {
        if (mProjectionManager != null) {
            startActivityForResult(
                    mProjectionManager.createScreenCaptureIntent(),
                    REQUEST_CODE_SCREEN_CAPTURE
            );
        }
    }

    private void startService() {
        Intent serviceIntent = new Intent(this, WakeUpService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void stopService() {
        Intent serviceIntent = new Intent(this, WakeUpService.class);
        stopService(serviceIntent);
    }

    private void onPorcupineInitError(final String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            stopService();
        });
    }

    public class ServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPorcupineInitError(intent.getStringExtra("errorMessage"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == Activity.RESULT_OK) {
                // 用户授权成功
                Toast.makeText(this, "屏幕捕获已授权，正在启动服务...", Toast.LENGTH_SHORT).show();

                // 启动后台服务，并将授权结果传递给它
                Intent serviceIntent = new Intent(this, ScreenCaptureService.class);
                serviceIntent.putExtra("resultCode", resultCode);
                serviceIntent.putExtra("data", data);

                startForegroundService(serviceIntent);
            } else {
                // 用户拒绝授权
                Toast.makeText(this, "用户拒绝屏幕捕获", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            onPorcupineInitError("Microphone/notification permissions are required for this demo");
        } else {
            startService();
        }
    }



}