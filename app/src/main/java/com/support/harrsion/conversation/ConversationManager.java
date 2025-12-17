package com.support.harrsion.conversation;

import android.content.Context;
import android.util.Log;

import com.support.harrsion.dto.conversation.Conversation;
import com.support.harrsion.dto.conversation.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationManager {
    private static final String TAG = "ConversationManager";
    private static final String CONVERSATIONS_FILE = "conversations.ser";
    
    private Context mContext;
    private List<Conversation> conversations;
    private Conversation currentConversation;
    
    public ConversationManager(Context context) {
        this.mContext = context;
        this.conversations = loadConversations();
        
        // 如果没有会话，则创建一个新会话
        if (conversations.isEmpty()) {
            this.currentConversation = createNewConversation();
        } else {
            // 默认使用最后一个会话
            this.currentConversation = conversations.get(conversations.size() - 1);
        }
    }
    
    /**
     * 创建新会话
     */
    public Conversation createNewConversation() {
        Conversation conversation = new Conversation();
        this.currentConversation = conversation;
        return conversation;
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
    }
    
    /**
     * 向当前会话添加消息
     */
    public void addMessageToCurrentConversation(String content, boolean isUser) {
        if (currentConversation != null) {
            Message message = new Message(content, isUser);
            currentConversation.addMessage(message);
            
            // 如果会话尚未在列表中，则添加到列表
            if (!conversations.contains(currentConversation)) {
                conversations.add(currentConversation);
            }
            
            saveConversations();
        }
    }
    
    /**
     * 获取所有会话
     */
    public List<Conversation> getAllConversations() {
        return conversations;
    }
    
    /**
     * 保存会话到文件
     */
    private void saveConversations() {
        try {
            FileOutputStream fos = mContext.openFileOutput(CONVERSATIONS_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(conversations);
            oos.close();
            fos.close();
            Log.d(TAG, "Conversations saved successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error saving conversations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从文件加载会话
     */
    private List<Conversation> loadConversations() {
        List<Conversation> loadedConversations = new ArrayList<>();
        
        try {
            FileInputStream fis = mContext.openFileInput(CONVERSATIONS_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            loadedConversations = (List<Conversation>) ois.readObject();
            ois.close();
            fis.close();
            Log.d(TAG, "Conversations loaded successfully: " + loadedConversations.size());
        } catch (FileNotFoundException e) {
            Log.d(TAG, "No existing conversations file found");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Error loading conversations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return loadedConversations;
    }
}