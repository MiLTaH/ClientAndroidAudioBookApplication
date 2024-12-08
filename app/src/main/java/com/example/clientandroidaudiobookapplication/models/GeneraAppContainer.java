package com.example.clientandroidaudiobookapplication.models;

import android.app.Application;
import okhttp3.OkHttpClient;

public class GeneraAppContainer extends Application {
    private String host;
    private OkHttpClient client;
    private String token;
    @Override
    public void onCreate() {
        super.onCreate();
        client = new OkHttpClient();
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
