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
import java.util.List;

public class MainActivity extends ComponentActivity {
    private DrawerLayout mDrawerLayout;
    private LinearLayout messagesContainer;
    private LinearLayout welcomeArea;
    private Context appContext;
    private ConversationManager conversationManager;
    private LinearLayout drawerSessionHistory;
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

        // 初始化会话管理器
        conversationManager = new ConversationManager(this);

        // 检查当前会话是否为空，如果不为空则不创建新会话
        Conversation currentConversation = conversationManager.getCurrentConversation();
        if (currentConversation.getMessages().isEmpty()) {
            // 当前会话为空，保持不变
        } else {
            // 创建新会话，确保每次打开app都是新会话
            conversationManager.createNewConversation();
        }

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
        
        // 监听回车键，处理为发送操作
        inputText.setOnKeyListener((v, keyCode, event) -> {
            // 检查是否是回车键并且是按下事件
            if (keyCode == android.view.KeyEvent.KEYCODE_ENTER && event.getAction() == android.view.KeyEvent.ACTION_DOWN) {
                // 检查是否同时按下了Shift键（如果按下Shift键，则允许换行）
                if (!event.isShiftPressed()) {
                    sendMessage(inputText);
                    return true;
                }
            }
            return false;
        });

        // 会话历史抽屉按钮点击事件
        sessionHistoryButton.setOnClickListener(v -> openSessionHistoryDrawer());

        // 点击聊天区域隐藏键盘
        setupKeyboardHiding();

        // Load logo image from assets
        loadLogoImage();

        SharedViewModel sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        // 观察UI状态的变化
        sharedViewModel.getUiState().observe(this, uiState -> {
            if (uiState != null) {
                welcomeArea.setVisibility(uiState.welcomeAreaVisible ? View.VISIBLE : View.GONE);
                displayMessage(uiState.message, false);
                
                // 将通知消息添加到当前会话，确保持久化
                if (uiState.message != null && !uiState.message.isEmpty()) {
                    conversationManager.addMessageToCurrentConversation(uiState.message, false);
                    updateSessionHistoryDrawer();
                }
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
            // 显示最近5条有消息的会话列表
            int maxHistoryItems = 5;
            int displayedCount = 0;
            
            for (int i = conversations.size() - 1; i >= 0 && displayedCount < maxHistoryItems; i--) {
                Conversation conversation = conversations.get(i);
                // 只显示有消息的会话
                if (!conversation.getMessages().isEmpty()) {
                    View sessionItem = createSessionHistoryItem(conversation);
                    drawerSessionHistory.addView(sessionItem);
                    displayedCount++;
                }
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
            mDrawerLayout.closeDrawer(Gravity.START);
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
            welcomeArea.setVisibility(View.GONE);
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
        try {
            if (mDrawerLayout != null && findViewById(R.id.drawer_session_history) != null) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error opening session history drawer: " + e.getMessage());
            e.printStackTrace();
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
     * 设置点击聊天区域隐藏键盘的功能
     */
    private void setupKeyboardHiding() {
        // 获取聊天区域的ScrollView和欢迎区域
        ScrollView scrollView = findViewById(R.id.content_scroll_view);
        LinearLayout welcomeArea = findViewById(R.id.welcome_area);
        LinearLayout messagesContainer = findViewById(R.id.messages_container);

        // 为ScrollView添加点击事件
        if (scrollView != null) {
            scrollView.setOnClickListener(v -> hideKeyboard());
        }

        // 为欢迎区域添加点击事件
        if (welcomeArea != null) {
            welcomeArea.setOnClickListener(v -> hideKeyboard());
        }

        // 为消息容器添加点击事件
        if (messagesContainer != null) {
            messagesContainer.setOnClickListener(v -> hideKeyboard());
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        try {
            EditText inputText = findViewById(R.id.input_text);
            if (inputText != null && inputText.isFocused()) {
                inputText.clearFocus();
            }
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MainActivity", "Error hiding keyboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}