package com.support.harrsion;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.Manifest;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.IOException;
import java.io.InputStream;
import androidx.lifecycle.ViewModelProvider;

import com.support.harrsion.broadcast.TaskBroadcastReceiver;
import com.support.harrsion.broadcast.WakeUpBroadcastReceiver;
import com.support.harrsion.service.AgentService;
import com.support.harrsion.utils.PermissionUtil;
import com.support.harrsion.viewModel.SharedViewModel;
import java.util.ArrayList;

public class MainActivity extends ComponentActivity {
    private DrawerLayout mDrawerLayout;
    private LinearLayout messagesContainer;
    private LinearLayout welcomeArea;
    private TaskBroadcastReceiver taskBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionUtil.requestScreenCapturePermission(this);

        ArrayList<String> permissionsToRequest = new ArrayList<>();
        if (!PermissionUtil.hasNotificationPermission(this)) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        if (!PermissionUtil.hasRecordPermission(this)) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }

        if (permissionsToRequest.isEmpty()) {
            WakeUpBroadcastReceiver.startService(this);
        } else {
            PermissionUtil.requestPermissions(this, permissionsToRequest.toArray(new String[0]));
        }

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

        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        // 观察UI状态的变化
        sharedViewModel.getUiState().observe(this, uiState -> {
            if (uiState != null) {
                welcomeArea.setVisibility(uiState.welcomeAreaVisible ? View.VISIBLE : View.GONE);
                displayMessage(uiState.message, false);
            }
        });

        // 检查辅助服务是否已启用
        if (!PermissionUtil.isAccessibilityServiceEnabled(this)) {
            PermissionUtil.openAccessibilitySettings(this);
        }

        taskBroadcastReceiver = new TaskBroadcastReceiver();
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(TaskBroadcastReceiver.ACTION_TASK_COMPLETED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(taskBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(taskBroadcastReceiver);
    }

    /**
     * 发送消息
     */
    private void sendMessage(EditText inputText) {
        String message = inputText.getText().toString().trim();
        if (!message.isEmpty()) {
            welcomeArea.setVisibility(View.GONE);
            displayMessage(message, true);
            inputText.setText("");
            Intent intent = new Intent(getApplicationContext(), AgentService.class);
            intent.putExtra("task", message);
            startForegroundService(intent);

        }
    }

    /**
     * 显示消息
     */
    public void displayMessage(String message, boolean isUser) {
        Log.d("MainActivity", "displayMessage called with message: " + message + ", isUser: " + isUser);
        Log.d("MainActivity", "messagesContainer before add: " + messagesContainer.getChildCount() + " children");
        Log.d("MainActivity", "messagesContainer visibility: " + messagesContainer.getVisibility());
        Log.d("MainActivity", "messagesContainer width: " + messagesContainer.getWidth() + ", height: " + messagesContainer.getHeight());
        Log.d("MainActivity", "welcomeArea visibility: " + welcomeArea.getVisibility());

        // 创建消息布局
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setGravity(isUser ? Gravity.END : Gravity.START);
        // 为不同类型的消息设置不同的布局内边距
        if (isUser) {
            // 用户消息（右对齐）：左边距大，右边距小
            messageLayout.setPadding(40, 8, 16, 8);
        } else {
            // AI消息（左对齐）：左边距小，右边距大
            messageLayout.setPadding(16, 8, 32, 8);
        }

        // 创建消息气泡
        TextView messageBubble = new TextView(this);
        messageBubble.setText(message);
        messageBubble.setTextSize(14);
        messageBubble.setTextColor(getResources().getColor(R.color.doubao_text_primary));

        // 设置不同的背景和内边距
        if (isUser) {
            messageBubble.setBackgroundResource(R.drawable.input_background);
            messageBubble.setPadding(20, 16, 20, 16);
        } else {
            // 为AI消息设置指定背景样式
            messageBubble.setBackgroundResource(R.drawable.ai_message_background);
            messageBubble.setTextColor(Color.BLACK);
            messageBubble.setPadding(20, 16, 20, 16);
        }

        // 添加到消息容器
        messageLayout.addView(messageBubble);
        // 所有消息都添加到末尾，保持时间顺序
        messagesContainer.addView(messageLayout);

        Log.d("MainActivity", "messagesContainer after add: " + messagesContainer.getChildCount() + " children");
        // 强制刷新布局
        messagesContainer.requestLayout();
        // 滚动到底部以显示最新消息
        ScrollView scrollView = (ScrollView) messagesContainer.getParent().getParent();
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        Log.d("MainActivity", "ScrollView scrolled to bottom");
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
}