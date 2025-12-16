package com.support.harrsion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.Manifest;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.IOException;
import java.io.InputStream;

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

    private DrawerLayout mDrawerLayout;
    private LinearLayout messagesContainer;
    private LinearLayout welcomeArea;

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

//        EditText taskInput = findViewById(R.id.input_text);

        // 初始化抽屉布局
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // 获取消息容器和欢迎区域
        messagesContainer = findViewById(R.id.messages_container);
        welcomeArea = findViewById(R.id.welcome_area);

        // 获取输入框和发送按钮
        EditText inputText = findViewById(R.id.input_text);
        Button sendButton = findViewById(R.id.btn_send);
        Button newSessionButton = findViewById(R.id.btn_new_session);
        ImageView sessionHistoryButton = findViewById(R.id.btn_session_history);

        // 新会话按钮点击事件
        newSessionButton.setOnClickListener(v -> createNewSession(inputText));

        // 发送按钮点击事件
        sendButton.setOnClickListener(v -> sendMessage(inputText));

        // 输入框回车键发送事件
        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage(inputText);
                return true;
            }
            return false;
        });

        // 会话历史抽屉按钮点击事件
        sessionHistoryButton.setOnClickListener(v -> openSessionHistoryDrawer());

        // Load logo image from assets
        loadLogoImage();

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
     * 发送消息
     */
    private void sendMessage(EditText inputText) {
        String message = inputText.getText().toString().trim();
        if (!message.isEmpty()) {
            // 隐藏欢迎区域
            welcomeArea.setVisibility(View.GONE);

            // 显示消息
            displayMessage(message, true);

            // 清空输入框
            inputText.setText("");
            Intent intent = new Intent(getApplicationContext(), AgentService.class);
            intent.putExtra("task", message);
            startForegroundService(intent);

        }
    }

    /**
     * 显示消息
     */
    private void displayMessage(String message, boolean isUser) {
        // 创建消息布局
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(isUser ? Gravity.END : Gravity.START);
        messageLayout.setPadding(16, 8, 16, 8);

        // 创建消息气泡
        TextView messageBubble = new TextView(this);
        messageBubble.setText(message);
        messageBubble.setTextSize(16);
        messageBubble.setTextColor(getResources().getColor(R.color.doubao_text_primary));

        // 设置不同的背景和内边距
        if (isUser) {
            messageBubble.setBackgroundResource(R.drawable.input_background);
            messageBubble.setPadding(16, 12, 16, 12);
        } else {
            messageBubble.setBackgroundResource(R.drawable.input_background);
            messageBubble.setPadding(16, 12, 16, 12);
        }

        // 添加到消息容器
        messageLayout.addView(messageBubble);
        // 所有消息都添加到末尾，保持时间顺序
        messagesContainer.addView(messageLayout);
    }

    /**
     * 创建新会话
     */
    private void createNewSession(EditText inputText) {
        String currentInput = inputText.getText().toString().trim();
        if (!currentInput.isEmpty()) {
            // 将当前输入记录添加到会话历史（模拟实现）
            Toast.makeText(this, "已将输入添加到会话历史: " + currentInput, Toast.LENGTH_SHORT).show();
        }
        // 清空当前输入
        inputText.setText("");
        // 清空消息容器
        messagesContainer.removeAllViews();
        // 显示欢迎区域
        welcomeArea.setVisibility(View.VISIBLE);
        // 提示用户已开始新会话
        Toast.makeText(this, "已开始新会话", Toast.LENGTH_SHORT).show();
    }

    /**
     * 打开会话历史抽屉
     */
    private void openSessionHistoryDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(R.id.drawer_session_history);
        }
    }

    /**
     * 从assets加载logo图片
     */
    private void loadLogoImage() {
        ImageView logoImage = findViewById(R.id.logo_image);
        try {
            // 从assets文件夹中打开图片文件
            InputStream inputStream = getAssets().open("logo.png");
            // 将输入流解码为Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // 设置图片到ImageView
            logoImage.setImageBitmap(bitmap);
            // 关闭输入流
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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