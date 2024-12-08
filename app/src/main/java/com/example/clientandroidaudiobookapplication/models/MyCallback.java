package com.example.clientandroidaudiobookapplication.models;
public interface MyCallback<T> {
    void onSuccess(T result);
    void onFailure(String errorMessage);
}
