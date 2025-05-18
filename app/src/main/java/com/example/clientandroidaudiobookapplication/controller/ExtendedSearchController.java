package com.example.clientandroidaudiobookapplication.controller;

import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.view.ExtendedSearchActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class ExtendedSearchController {
    private final ExtendedSearchActivity activity;

    public ExtendedSearchController(ExtendedSearchActivity activity) {
        this.activity = activity;
    }

    public void findBooksByTitle(MyCallback<List<FindBooksResponse>> callback, GeneraAppContainer app) {
        String titleQuery = activity.getSearchInput();
        if (titleQuery.isEmpty()) {
            callback.onFailure("Введите название книги для поиска.");
            return;
        }

        HttpUrl url = HttpUrl.parse(app.getHost() + "/librivox/search/title")
                .newBuilder()
                .addQueryParameter("title", titleQuery)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + app.getToken())
                .build();

        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                activity.runOnUiThread(() -> callback.onFailure("Ошибка запроса: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    activity.runOnUiThread(() -> callback.onFailure("Пустой ответ от сервера."));
                    return;
                }

                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<FindBooksResponse>>() {}.getType();
                        List<FindBooksResponse> books = new Gson().fromJson(responseBody, listType);
                        activity.runOnUiThread(() -> callback.onSuccess(books));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> callback.onFailure("Ошибка обработки данных: " + e.getMessage()));
                    }
                } else {
                    activity.runOnUiThread(() -> callback.onFailure("Ошибка сервера: " + response.code()));
                }
            }
        });
    }
}

