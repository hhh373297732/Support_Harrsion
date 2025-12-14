package com.support.harrsion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.support.harrsion.service.AgentService;
import com.support.harrsion.service.AccessibilityService;
import com.support.harrsion.service.ScreenCaptureService;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1001;
    private MediaProjectionManager mProjectionManager;// 确保和 Service 中的定义一致

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 假设您的布局文件中有一个ID为 btn_start_capture 的按钮
        setContentView(R.layout.activity_main);

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        requestScreenCapturePermission();

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
                AccessibilityService.class.getCanonicalName();

        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );

        return enabledServices != null && enabledServices.contains(service);
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

                // 启动前台服务 (Android O及以上)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent);
                } else {
                    startService(serviceIntent);
                }
            } else {
                // 用户拒绝授权
                Toast.makeText(this, "用户拒绝屏幕捕获", Toast.LENGTH_SHORT).show();
            }
        }
    }


}