package com.dds.textrecognition.Model;

public class User {
    private String userId;
    private String password;
    private int lastWordId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLastWordId() {
        return lastWordId;
    }

    public void setLastWordId(int lastWordId) {
        this.lastWordId = lastWordId;
    }
}
