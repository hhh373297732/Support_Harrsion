package com.support.harrsion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.support.harrsion.service.ScreenCaptureService;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1001;
    private MediaProjectionManager mProjectionManager;
    private Button startCaptureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 假设您的布局文件中有一个ID为 btn_start_capture 的按钮
        setContentView(R.layout.activity_main);

        startCaptureButton = findViewById(R.id.btn_start_capture);
        startCaptureButton.setOnClickListener(v -> requestScreenCapturePermission());

        // 获取 MediaProjectionManager 实例
        mProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    /**
     * 启动系统 Intent，请求屏幕捕获权限。
     */
    private void requestScreenCapturePermission() {
        if (mProjectionManager != null) {
            // 这将弹出“允许应用捕获屏幕”的系统对话框
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