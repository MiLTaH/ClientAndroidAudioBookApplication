package com.example.clientandroidaudiobookapplication.models;

public class SubscribeRequest {
    private final String userName;
    private final String bookName;
    public SubscribeRequest(String userName, String bookName) {
        this.userName = userName;
        this.bookName = bookName;
    }
    public String getUserName() {
        return userName;
    }
    public String getBookName() {
        return bookName;
    }
}
