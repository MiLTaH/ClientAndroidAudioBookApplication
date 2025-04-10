package com.example.clientandroidaudiobookapplication.models;

import android.app.Application;

import java.util.List;

import okhttp3.OkHttpClient;

public class GeneraAppContainer extends Application {
    private String host;
    private OkHttpClient client;
    private String token;
    private String Username;

    @Override
    public void onCreate() {
        super.onCreate();
        client = new OkHttpClient();
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


    public OkHttpClient getClient() {
        return client;
    }

}
