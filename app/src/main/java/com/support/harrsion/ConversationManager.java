package com.support.harrsion;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ConversationManager {
    private static final String TAG = "ConversationManager";
    private static final String CONVERSATIONS_FILE = "conversations.dat";
    private Context context;
    private List<Conversation> conversations;
    private Conversation currentConversation;
    
    public ConversationManager(Context context) {
        this.context = context;
        this.conversations = loadConversations();
        if (this.conversations == null) {
            this.conversations = new ArrayList<>();
        }
        // 如果没有会话，创建一个新会话
        if (this.conversations.isEmpty()) {
            this.currentConversation = new Conversation();
            this.conversations.add(currentConversation);
            saveConversations();
        } else {
            // 默认使用最后一个会话
            this.currentConversation = this.conversations.get(this.conversations.size() - 1);
        }
    }
    
    /**
     * 加载所有会话
     */
    @SuppressWarnings("unchecked")
    private List<Conversation> loadConversations() {
        try (FileInputStream fis = context.openFileInput(CONVERSATIONS_FILE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<Conversation>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "加载会话失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 保存所有会话
     */
    private void saveConversations() {
        try (FileOutputStream fos = context.openFileOutput(CONVERSATIONS_FILE, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(conversations);
            Log.d(TAG, "会话保存成功");
        } catch (IOException e) {
            Log.e(TAG, "保存会话失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前会话
     */
    public Conversation getCurrentConversation() {
        return currentConversation;
    }
    
    /**
     * 设置当前会话
     */
    public void setCurrentConversation(Conversation conversation) {
        this.currentConversation = conversation;
        saveConversations();
    }
    
    /**
     * 获取所有会话
     */
    public List<Conversation> getAllConversations() {
        return conversations;
    }
    
    /**
     * 创建新会话
     */
    public Conversation createNewConversation() {
        Conversation newConversation = new Conversation();
        conversations.add(newConversation);
        currentConversation = newConversation;
        saveConversations();
        return newConversation;
    }
    
    /**
     * 删除会话
     */
    public boolean deleteConversation(String conversationId) {
        for (int i = 0; i < conversations.size(); i++) {
            if (conversations.get(i).getId().equals(conversationId)) {
                conversations.remove(i);
                // 如果删除的是当前会话，切换到最近的会话
                if (currentConversation.getId().equals(conversationId)) {
                    if (conversations.isEmpty()) {
                        currentConversation = createNewConversation();
                    } else {
                        currentConversation = conversations.get(conversations.size() - 1);
                    }
                }
                saveConversations();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 向当前会话添加消息
     */
    public void addMessageToCurrentConversation(String content, boolean isUser) {
        Message message = new Message(content, isUser);
        currentConversation.addMessage(message);
        saveConversations();
    }
    
    /**
     * 清空当前会话
     */
    public void clearCurrentConversation() {
        currentConversation.getMessages().clear();
        currentConversation.setTitle("新会话");
        saveConversations();
    }
}