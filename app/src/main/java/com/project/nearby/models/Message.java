package com.project.nearby.models;

public class Message {
    String userId;
    String body;
    boolean delivered;

    public Message() {
    }

    public Message(String userId, String body, boolean delivered) {
        this.userId = userId;
        this.body = body;
        this.delivered = delivered;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public String getUserId() {
        return userId;
    }

    public String getBody() {
        return body;
    }

    public boolean isDelivered() {
        return delivered;
    }
}
