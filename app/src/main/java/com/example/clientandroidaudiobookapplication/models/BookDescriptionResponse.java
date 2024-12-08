package com.example.clientandroidaudiobookapplication.models;

public class BookDescriptionResponse {

    private String description;
    private String bookImageUrl;

    public BookDescriptionResponse(String description, String bookImageUrl) {
        this.description = description;
        this.bookImageUrl = bookImageUrl;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
