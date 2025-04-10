package com.example.clientandroidaudiobookapplication.controller;

import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.view.RecommendedActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RecommendedController {
    private final RecommendedActivity recommendedActivity;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public RecommendedController(RecommendedActivity recommendedActivity) {
        this.recommendedActivity = recommendedActivity;
    }

    public void getBookGenres(String bookName, MyCallback<List<String>> callback, GeneraAppContainer app) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("bookName", bookName);
            Request request = new Request.Builder()
                    .url(app.getHost() + "/genre/getBookGenre")  // Точный путь к эндпоинту
                    .addHeader("Authorization", "Bearer " + app.getToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toString(), JSON))
                    .build();
            app.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    recommendedActivity.runOnUiThread(() ->
                            callback.onFailure("Ошибка соединения: " + e.getMessage()));
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String errorBody = response.body() != null ? response.body().string() : "empty body";
                            recommendedActivity.runOnUiThread(() ->
                                    callback.onFailure("HTTP error: " + response.code() + ", " + errorBody));
                            return;
                        }
                        String responseBody = response.body().string();
                        JSONArray jsonArray = new JSONArray(responseBody);
                        List<String> genres = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            genres.add(jsonArray.getString(i));
                        }
                        recommendedActivity.runOnUiThread(() -> callback.onSuccess(genres));
                    } catch (Exception e) {
                        recommendedActivity.runOnUiThread(() ->
                                callback.onFailure("Ошибка обработки ответа: " + e.getMessage()));
                    }
                }
            });
        } catch (JSONException e) {
            recommendedActivity.runOnUiThread(() ->
                    callback.onFailure("Ошибка формирования запроса: " + e.getMessage()));
        }
    }
    public void getRecommendedBooks(List<String> genres, MyCallback<List<FindBooksResponse>> callback, GeneraAppContainer app) {
        try {
            JSONArray genresArray = new JSONArray(genres);

            Request request = new Request.Builder()
                    .url(app.getHost() + "/books/showRecommendBooks")
                    .addHeader("Authorization", "Bearer " + app.getToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(genresArray.toString(), JSON))
                    .build();
            app.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    recommendedActivity.runOnUiThread(() ->
                            callback.onFailure("Ошибка соединения: " + e.getMessage()));
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) {
                            String errorBody = response.body() != null ? response.body().string() : "empty body";
                            recommendedActivity.runOnUiThread(() ->
                                    callback.onFailure("HTTP error: " + response.code() + ", " + errorBody));
                            return;
                        }
                        String responseBody = response.body().string();
                        Type listType = new TypeToken<List<FindBooksResponse>>(){}.getType();
                        List<FindBooksResponse> books = new Gson().fromJson(responseBody, listType);
                        recommendedActivity.runOnUiThread(() -> callback.onSuccess(books));
                    } catch (Exception e) {
                        recommendedActivity.runOnUiThread(() ->
                                callback.onFailure("Ошибка обработки ответа: " + e.getMessage()));
                    }
                }
            });
        } catch (Exception e) {
            recommendedActivity.runOnUiThread(() ->
                    callback.onFailure("Ошибка формирования запроса: " + e.getMessage()));
        }
    }
    public void showVoiceSelectionDialog(MyCallback<List<ActorVoicesResponse>> callback,
                                         FindBooksResponse book,
                                         GeneraAppContainer app) {
        Request request = new Request.Builder()
                .url(app.getHost() + "/books/" + book.getId() + "/voices")
                .addHeader("Authorization", "Bearer " + app.getToken())
                .addHeader("Content-Type", "application/json")
                .build();
        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                recommendedActivity.runOnUiThread(() ->
                        callback.onFailure("Ошибка соединения: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "empty body";
                        recommendedActivity.runOnUiThread(() ->
                                callback.onFailure("HTTP error: " + response.code() + ", " + errorBody));
                        return;
                    }
                    String responseBody = response.body().string();
                    Type listType = new TypeToken<List<ActorVoicesResponse>>(){}.getType();
                    List<ActorVoicesResponse> voices = new Gson().fromJson(responseBody, listType);
                    recommendedActivity.runOnUiThread(() -> callback.onSuccess(voices));
                } catch (Exception e) {
                    recommendedActivity.runOnUiThread(() ->
                            callback.onFailure("Ошибка обработки ответа: " + e.getMessage()));
                }
            }
        });
    }
}