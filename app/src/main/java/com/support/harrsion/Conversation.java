package com.support.harrsion;

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
    
    public Conversation(String title) {
        this.id = generateId();
        this.messages = new ArrayList<>();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.title = title;
    }
    
    private String generateId() {
        return "conv_" + System.currentTimeMillis();
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        this.updatedAt = new Date();
        // 更新会话标题为最新的用户消息
        if (message.isUser() && message.getContent().length() > 0) {
            this.title = message.getContent().length() > 20 ? 
                message.getContent().substring(0, 20) + "..." : 
                message.getContent();
        }
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
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
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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
    
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
}