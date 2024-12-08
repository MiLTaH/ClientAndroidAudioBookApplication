package com.example.clientandroidaudiobookapplication.models;

import com.google.gson.annotations.SerializedName;

public class LoginRegResponse {
    @SerializedName("jwt-token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

