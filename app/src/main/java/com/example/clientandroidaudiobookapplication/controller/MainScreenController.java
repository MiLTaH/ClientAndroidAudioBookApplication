package com.example.clientandroidaudiobookapplication.controller;

import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.view.MainScreenActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainScreenController {
    MainScreenActivity mainScreenActivity;

    public MainScreenController(MainScreenActivity mainScreenActivity) {
        this.mainScreenActivity = mainScreenActivity;
    }

    public void findBooks(MyCallback<List<FindBooksResponse>> callback, GeneraAppContainer app) {
        String searchQuery = mainScreenActivity.editText.getText().toString().trim();
        if (searchQuery.isEmpty()) {
            callback.onFailure("Введите название книги для поиска.");
            return;
        }
        RequestBody requestBody = new FormBody.Builder()
                .add("bookname", searchQuery)
                .build();
        Request request = new Request.Builder()
                .url(app.getHost() + "/books/show")
                .addHeader("Authorization", "Bearer " + app.getToken())
                .post(requestBody)
                .build();

        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка запроса: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    mainScreenActivity.runOnUiThread(() -> callback.onFailure("Пустой ответ от сервера."));
                    return;
                }
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<FindBooksResponse>>() {}.getType();
                        List<FindBooksResponse> books = new Gson().fromJson(responseBody, listType);
                        mainScreenActivity.runOnUiThread(() -> callback.onSuccess(books));
                    } catch (Exception e) {
                        mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка обработки данных: " + e.getMessage()));
                    }
                } else {
                    mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка сервера: " + response.code()));
                }
            }
        });
    }
    public void showVoiceSelectionDialog(MyCallback<List<ActorVoicesResponse>> callback, FindBooksResponse book, GeneraAppContainer app) {
        Request request = new Request.Builder()
                .url(app.getHost() + "/books" + "/" + book.getId() +  "/voices")
                .addHeader("Authorization", "Bearer " + app.getToken())
                .build();
        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка запроса: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    mainScreenActivity.runOnUiThread(() -> callback.onFailure("Пустой ответ от сервера."));
                    return;
                }
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<ActorVoicesResponse>>() {}.getType();
                        List<ActorVoicesResponse> actorVoices = new Gson().fromJson(responseBody, listType);
                        mainScreenActivity.runOnUiThread(() -> callback.onSuccess(actorVoices));
                    } catch (Exception e) {
                        mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка обработки данных: " + e.getMessage()));
                    }
                } else {
                    mainScreenActivity.runOnUiThread(() -> callback.onFailure("Ошибка сервера: " + response.code()));
                }
            }
        });
    }
}
