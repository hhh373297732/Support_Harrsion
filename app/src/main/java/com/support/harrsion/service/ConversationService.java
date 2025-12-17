package com.support.harrsion.service;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import androidx.core.view.GravityCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.drawerlayout.widget.DrawerLayout;
import com.support.harrsion.conversation.ConversationManager;
import com.support.harrsion.dto.conversation.Conversation;
import com.support.harrsion.dto.conversation.Message;
import com.support.harrsion.MainActivity;

import java.util.List;

/**
 * 会话管理服务
 * 处理会话的创建、消息发送、历史记录管理等功能
 *
 * @author harrsion
 * @date 2025/12/17
 */
public class ConversationService {
    private Context context;
    private ConversationManager conversationManager;
    private DrawerLayout drawerLayout;
    private LinearLayout sessionHistoryContainer;
    private MainActivity mainActivity;

    public ConversationService(Context context, DrawerLayout drawerLayout, LinearLayout sessionHistoryContainer, MainActivity mainActivity) {
        this.context = context;
        this.drawerLayout = drawerLayout;
        this.sessionHistoryContainer = sessionHistoryContainer;
        this.mainActivity = mainActivity;
        this.conversationManager = new ConversationManager(context);
    }

    /**
     * 初始化会话
     */
    public void initConversation() {
        Conversation currentConversation = conversationManager.getCurrentConversation();
        if (!currentConversation.getMessages().isEmpty()) {
            conversationManager.createNewConversation();
        }
    }

    /**
     * 获取会话管理器
     */
    public ConversationManager getConversationManager() {
        return conversationManager;
    }

    /**
     * 更新会话历史抽屉
     */
    public void updateSessionHistoryDrawer() {
        List<Conversation> conversations = conversationManager.getAllConversations();
        sessionHistoryContainer.removeAllViews();

        if (conversations.isEmpty()) {
            mainActivity.showEmptySessionHistory(sessionHistoryContainer);
        } else {
            int maxHistoryItems = 5;
            int displayedCount = 0;
            
            for (int i = conversations.size() - 1; i >= 0 && displayedCount < maxHistoryItems; i--) {
                Conversation conversation = conversations.get(i);
                if (!conversation.getMessages().isEmpty()) {
                    View sessionItem = createSessionHistoryItem(conversation);
                    sessionHistoryContainer.addView(sessionItem);
                    displayedCount++;
                }
            }
        }
    }

    /**
     * 创建会话历史项视图
     */
    private View createSessionHistoryItem(Conversation conversation) {
        return mainActivity.createSessionHistoryItem(conversation);
    }

    /**
     * 创建新会话
     */
    public void createNewSession(String currentInput) {
        if (!currentInput.isEmpty()) {
            conversationManager.addMessageToCurrentConversation(currentInput, true);
            mainActivity.displayMessage(currentInput, true);
            updateSessionHistoryDrawer();
        }
        conversationManager.createNewConversation();
        updateSessionHistoryDrawer();
    }

    /**
     * 打开会话历史抽屉
     */
    public void openSessionHistoryDrawer() {
        try {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        } catch (Exception e) {
            Log.e("ConversationService", "Error opening session history drawer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 添加消息到当前会话
     */
    public void addMessageToCurrentConversation(String message, boolean isUser) {
        conversationManager.addMessageToCurrentConversation(message, isUser);
        updateSessionHistoryDrawer();
    }

    /**
     * 设置当前会话
     */
    public void setCurrentConversation(Conversation conversation) {
        conversationManager.setCurrentConversation(conversation);
    }

    /**
     * 获取当前会话
     */
    public Conversation getCurrentConversation() {
        return conversationManager.getCurrentConversation();
    }

    /**
     * 加载当前会话的消息
     */
    public void loadCurrentConversationMessages() {
        mainActivity.loadCurrentConversationMessages();
    }
}