package com.example.clientandroidaudiobookapplication.controller;

import com.example.clientandroidaudiobookapplication.models.LoginRegResponse;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.view.MainActivity;
import com.google.gson.Gson;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainController {
    private MainActivity mainActivity;
    public MainController(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
    public void login(MyCallback callback, GeneraAppContainer app) {
        RequestBody postBody = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                "{ \"username\": \"" + mainActivity.getEditLogin().getText() + "\"," +
                        " \"password\": \"" + mainActivity.getEditPassword().getText() + "\" }"
        );
        Request postRequest = new Request.Builder()
                .url(app.getHost() + "/auth/login")
                .post(postBody)
                .build();
        app.getClient().newCall(postRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainActivity.runOnUiThread(() -> callback.onFailure("Произошла ошибка при выполнении запроса: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        Gson gson = new Gson();
                        LoginRegResponse serverResponse = gson.fromJson(responseBody, LoginRegResponse.class);
                        if (serverResponse != null && serverResponse.getToken() != null) {
                            mainActivity.runOnUiThread(() -> callback.onSuccess(serverResponse));
                        } else {
                            mainActivity.runOnUiThread(() -> callback.onFailure("Ответ не содержит токен."));
                        }
                    } catch (Exception e) {
                        mainActivity.runOnUiThread(() -> callback.onFailure("Ошибка при обработке данных: " + e.getMessage()));
                    }
                } else {
                    mainActivity.runOnUiThread(() -> callback.onFailure("Ошибка на сервере: " + response.code()));
                }
            }
        });
    }
}
