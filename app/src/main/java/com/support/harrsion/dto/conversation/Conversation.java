package com.support.harrsion.dto.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String title;
    private Date createdAt;
    private Date updatedAt;
    private List<Message> messages;
    
    public Conversation() {
        this.id = generateId();
        this.messages = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.title = "新会话";
    }
    
    private String generateId() {
        return "conv_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<Message> getMessages() {
        return messages;
    }
    
    public void addMessage(Message message) {
        this.messages.add(message);
        this.updatedAt = new Date();
        
        // 如果会话标题是默认的，且是用户消息，则用该消息内容作为会话标题
        if ("新会话".equals(this.title) && message.isUser()) {
            String newTitle = message.getContent();
            if (newTitle.length() > 20) {
                newTitle = newTitle.substring(0, 20) + "...";
            }
            this.title = newTitle;
        }
    }
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}