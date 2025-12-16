package com.support.harrsion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
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
import android.widget.ScrollView;
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
import java.util.List;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1001;
    private static final int REQUEST_CODE_AUDIO = 1002;
    private MediaProjectionManager mProjectionManager;

    private DrawerLayout mDrawerLayout;
    private LinearLayout messagesContainer;
    private LinearLayout welcomeArea;
    private Context appContext;
    private ConversationManager conversationManager;
    private LinearLayout drawerSessionHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化应用上下文
        appContext = getApplicationContext();
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

        // 初始化会话管理器
        conversationManager = new ConversationManager(this);
        
        // 获取输入框和发送按钮
        EditText inputText = findViewById(R.id.input_text);
        Button sendButton = findViewById(R.id.btn_send);
        Button newSessionButton = findViewById(R.id.btn_new_session);
        ImageView sessionHistoryButton = findViewById(R.id.btn_session_history);
        
        // 获取会话历史抽屉
        drawerSessionHistory = findViewById(R.id.drawer_session_history_content);
        
        // 加载当前会话的消息
        loadCurrentConversationMessages();
        
        // 更新会话历史抽屉
        updateSessionHistoryDrawer();

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
     * 加载当前会话的消息
     */
    private void loadCurrentConversationMessages() {
        Conversation currentConversation = conversationManager.getCurrentConversation();
        if (currentConversation != null && !currentConversation.getMessages().isEmpty()) {
            welcomeArea.setVisibility(View.GONE);
            messagesContainer.removeAllViews();
            for (Message message : currentConversation.getMessages()) {
                displayMessage(message.getContent(), message.isUser());
            }
        } else {
            welcomeArea.setVisibility(View.VISIBLE);
            messagesContainer.removeAllViews();
        }
    }
    
    /**
     * 更新会话历史抽屉
     */
    private void updateSessionHistoryDrawer() {
        List<Conversation> conversations = conversationManager.getAllConversations();
        drawerSessionHistory.removeAllViews();
        
        if (conversations.isEmpty()) {
            // 显示空状态
            ImageView emptyIcon = new ImageView(this);
            emptyIcon.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
            emptyIcon.setImageResource(android.R.drawable.ic_dialog_info);
            emptyIcon.setAlpha(0.5f);
            emptyIcon.setPadding(0, 24, 0, 24);
            drawerSessionHistory.addView(emptyIcon);
            
            TextView emptyTitle = new TextView(this);
            emptyTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            emptyTitle.setText("暂无会话历史");
            emptyTitle.setTextColor(getResources().getColor(R.color.doubao_text_secondary));
            emptyTitle.setTextSize(16);
            emptyTitle.setGravity(Gravity.CENTER);
            emptyTitle.setPadding(0, 0, 0, 8);
            drawerSessionHistory.addView(emptyTitle);
            
            TextView emptySubtitle = new TextView(this);
            emptySubtitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            emptySubtitle.setText("开始新会话来添加历史记录");
            emptySubtitle.setTextColor(getResources().getColor(R.color.doubao_text_secondary));
            emptySubtitle.setTextSize(14);
            emptySubtitle.setGravity(Gravity.CENTER);
            emptySubtitle.setLineSpacing(4, 1);
            drawerSessionHistory.addView(emptySubtitle);
        } else {
            // 显示会话列表
            for (int i = conversations.size() - 1; i >= 0; i--) {
                Conversation conversation = conversations.get(i);
                View sessionItem = createSessionHistoryItem(conversation);
                drawerSessionHistory.addView(sessionItem);
            }
        }
    }
    
    /**
     * 创建会话历史项视图
     */
    private View createSessionHistoryItem(Conversation conversation) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        itemLayout.setPadding(16, 12, 16, 12);
        itemLayout.setBackgroundResource(R.drawable.input_background);
        itemLayout.setClickable(true);
        itemLayout.setFocusable(true);
        // 设置点击效果
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        itemLayout.setForeground(getResources().getDrawable(outValue.resourceId, getTheme()));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemLayout.getLayoutParams();
        params.setMargins(4, 0, 4, 8);
        itemLayout.setLayoutParams(params);
        
        // 添加点击事件
        itemLayout.setOnClickListener(v -> {
            // 切换到选中的会话
            conversationManager.setCurrentConversation(conversation);
            // 加载选中会话的消息
            loadCurrentConversationMessages();
            // 关闭抽屉
            mDrawerLayout.closeDrawer(R.id.drawer_session_history);
        });
        
        // 会话标题
        TextView titleText = new TextView(this);
        titleText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        titleText.setText(conversation.getTitle());
        titleText.setTextColor(getResources().getColor(R.color.doubao_text_primary));
        titleText.setTextSize(16);
        titleText.setMaxLines(1);
        titleText.setEllipsize(android.text.TextUtils.TruncateAt.END);
        itemLayout.addView(titleText);
        
        // 会话时间
        TextView timeText = new TextView(this);
        timeText.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        timeText.setText(formatDateTime(conversation.getUpdatedAt()));
        timeText.setTextColor(getResources().getColor(R.color.doubao_text_secondary));
        timeText.setTextSize(12);
        timeText.setPadding(0, 4, 0, 0);
        itemLayout.addView(timeText);
        
        return itemLayout;
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(java.util.Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
        return sdf.format(date);
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
            
            // 将消息添加到当前会话
            conversationManager.addMessageToCurrentConversation(message, true);

            // 清空输入框
            inputText.setText("");
            
            // 更新会话历史抽屉
            updateSessionHistoryDrawer();
            
            Intent intent = new Intent(getApplicationContext(), AgentService.class);
            intent.putExtra("task", message);
            startForegroundService(intent);

        }
    }

    /**
     * 显示消息
     */
    private void displayMessage(String message, boolean isUser) {
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
            // 将当前输入记录添加到当前会话
            conversationManager.addMessageToCurrentConversation(currentInput, true);
            // 显示消息
            displayMessage(currentInput, true);
            // 更新会话历史抽屉
            updateSessionHistoryDrawer();
        }
        
        // 创建新会话
        conversationManager.createNewConversation();
        
        // 清空当前输入
        inputText.setText("");
        // 清空消息容器
        messagesContainer.removeAllViews();
        // 显示欢迎区域
        welcomeArea.setVisibility(View.VISIBLE);
        // 提示用户已开始新会话
        Toast.makeText(this, "已开始新会话", Toast.LENGTH_SHORT).show();
        
        // 更新会话历史抽屉
        updateSessionHistoryDrawer();
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
     * 广播接收器，用于接收Agent任务完成的通知
     */
    private BroadcastReceiver agentTaskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "收到广播，action: " + intent.getAction());
            if ("com.support.harrsion.AGENT_TASK_COMPLETED".equals(intent.getAction())) {
                String resultMessage = intent.getStringExtra("result_message");
                Log.d("MainActivity", "收到任务完成消息: " + resultMessage);
                // 在主线程更新UI
            runOnUiThread(() -> {
                Log.d("MainActivity", "准备显示任务完成消息");
                // 隐藏欢迎区域，确保消息可见
                welcomeArea.setVisibility(View.GONE);
                displayMessage(resultMessage, false);
                // 将AI回复添加到当前会话
                conversationManager.addMessageToCurrentConversation(resultMessage, false);
                // 更新会话历史抽屉
                updateSessionHistoryDrawer();
                Log.d("MainActivity", "任务完成消息已显示");
            });
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // 注册全局广播接收器
        IntentFilter filter = new IntentFilter("com.support.harrsion.AGENT_TASK_COMPLETED");
        registerReceiver(agentTaskReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        Log.d("MainActivity", "全局广播接收器已注册");
        Log.d("MainActivity", "广播接收器Action: com.support.harrsion.AGENT_TASK_COMPLETED");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 注销全局广播接收器
        unregisterReceiver(agentTaskReceiver);
        Log.d("MainActivity", "全局广播接收器已注销");
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