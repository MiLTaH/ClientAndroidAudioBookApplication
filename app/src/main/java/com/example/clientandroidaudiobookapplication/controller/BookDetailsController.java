package com.example.clientandroidaudiobookapplication.controller;

import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.BookDescriptionResponse;
import com.example.clientandroidaudiobookapplication.models.ChapterResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.SubscribeRequest;
import com.example.clientandroidaudiobookapplication.view.BookDetailsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookDetailsController {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final BookDetailsActivity activity;
    private final Gson gson = new Gson();

    public BookDetailsController(BookDetailsActivity activity) {
        this.activity = activity;
    }
    public void loadImage(String imageUrl, ImageView imageView) {
        activity.runOnUiThread(() ->
                Glide.with(activity)
                        .load(imageUrl)
                        .override(1024, 1024)
                        .into(imageView)
        );
    }
    public void fetchChapters(MyCallback<List<ChapterResponse>> callback,
                              FindBooksResponse book,
                              ActorVoicesResponse actorVoice,
                              GeneraAppContainer app) {
        String url = String.format("%s/chapters/%d/%d", app.getHost(), book.getId(), actorVoice.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<List<ChapterResponse>>(){}.getType(), callback);
    }
    public void getBookImage(MyCallback<BookDescriptionResponse> callback,
                             FindBooksResponse book,
                             GeneraAppContainer app) {
        String url = String.format("%s/books/%d/description", app.getHost(), book.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<BookDescriptionResponse>(){}.getType(), callback);
    }
    public void subscribeOrCancelBook(MyCallback<String> callback,
                                      String userName,
                                      String bookName,
                                      GeneraAppContainer app) {
        try {
            String jsonBody = String.format("{\n" +
                    "  \"userName\": \"%s\",\n" +
                    "  \"bookName\": \"%s\"\n" +
                    "}", userName, bookName);
            Log.println(Log.ASSERT, String.valueOf(1), userName);
            Request request = new Request.Builder()
                    .url(app.getHost() + "/user/subscribeUserToBook")
                    .addHeader("Authorization", "Bearer " + app.getToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();
            app.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    activity.runOnUiThread(() ->
                            callback.onFailure("Network error: " + e.getMessage()));
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    activity.runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            callback.onSuccess(responseBody);
                        } else {
                            callback.onFailure("Server error " + response.code() + ": " + responseBody);
                        }
                    });
                }
            });
        } catch (Exception e) {
            activity.runOnUiThread(() ->
                    callback.onFailure("Request creation error: " + e.getMessage()));
        }
    }
    public void updateChapters(List<ChapterResponse> chapters, LinearLayout chaptersLayout) {
        activity.runOnUiThread(() -> {
            chaptersLayout.removeAllViews();

            for (ChapterResponse chapter : chapters) {
                LinearLayout chapterRow = createChapterRow(chapter);
                chaptersLayout.addView(chapterRow);
                setupChapterPlayer(chapter, chapterRow);
            }
        });
    }

    private Request createAuthorizedRequest(String url, String token) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    private <T> void executeRequest(Request request, Type type, MyCallback<T> callback) {
        GeneraAppContainer app = (GeneraAppContainer) activity.getApplication();
        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyFailure(callback, "Ошибка запроса: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    notifyFailure(callback, "Пустой ответ от сервера.");
                    return;
                }
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        T result = gson.fromJson(responseBody, type);
                        notifySuccess(callback, result);
                    } catch (Exception e) {
                        notifyFailure(callback, "Ошибка обработки данных: " + e.getMessage());
                    }
                } else {
                    notifyFailure(callback, "Ошибка сервера: " + response.code());
                }
            }
        });
    }

    private void executeStringRequest(Request request, MyCallback<String> callback) {
        GeneraAppContainer app = (GeneraAppContainer) activity.getApplication();
        app.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notifyFailure(callback, "Ошибка запроса: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    notifyFailure(callback, "Пустой ответ от сервера.");
                    return;
                }

                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    notifySuccess(callback, responseBody);
                } else {
                    notifyFailure(callback, "Ошибка сервера: " + response.code());
                }
            }
        });
    }
    private <T> void notifySuccess(MyCallback<T> callback, T result) {
        activity.runOnUiThread(() -> callback.onSuccess(result));
    }
    private <T> void notifyFailure(MyCallback<T> callback, String error) {
        activity.runOnUiThread(() -> callback.onFailure(error));
    }
    private LinearLayout createChapterRow(ChapterResponse chapter) {
        LinearLayout chapterRow = new LinearLayout(activity);
        chapterRow.setOrientation(LinearLayout.VERTICAL);
        chapterRow.setPadding(0, 16, 0, 16);
        TextView chapterTitle = new TextView(activity);
        chapterTitle.setText(chapter.getNameChapter());
        chapterTitle.setTextSize(16);
        chapterTitle.setPadding(16, 8, 16, 8);
        LinearLayout controlsLayout = new LinearLayout(activity);
        controlsLayout.setOrientation(LinearLayout.HORIZONTAL);
        Button playPauseButton = new Button(activity);
        playPauseButton.setText("▶️");
        playPauseButton.setPadding(16, 8, 16, 8);
        SeekBar seekBar = new SeekBar(activity);
        seekBar.setPadding(16, 8, 16, 8);
        seekBar.setMax(100);
        controlsLayout.addView(playPauseButton);
        controlsLayout.addView(seekBar);
        chapterRow.addView(chapterTitle);
        chapterRow.addView(controlsLayout);
        return chapterRow;
    }
    private void setupChapterPlayer(ChapterResponse chapter, LinearLayout chapterRow) {
        LinearLayout controlsLayout = (LinearLayout) chapterRow.getChildAt(1);
        Button playPauseButton = (Button) controlsLayout.getChildAt(0);
        SeekBar seekBar = (SeekBar) controlsLayout.getChildAt(1);
        MediaPlayer mediaPlayer = new MediaPlayer();
        playPauseButton.setOnClickListener(v -> togglePlayback(mediaPlayer, playPauseButton, chapter));
        setupMediaPlayerListeners(mediaPlayer, playPauseButton, seekBar);
        setupSeekBarListener(mediaPlayer, seekBar);
        startProgressUpdater(mediaPlayer, seekBar);
    }
    private void togglePlayback(MediaPlayer mediaPlayer, Button button, ChapterResponse chapter) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            button.setText("▶️");
        } else {
            try {
                if (mediaPlayer.getCurrentPosition() == 0) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(chapter.getChapterUrl());
                    mediaPlayer.prepareAsync();
                } else {
                    mediaPlayer.start();
                    button.setText("⏸️");
                }
            } catch (IOException e) {
                notifyFailure(null, "Ошибка воспроизведения: " + e.getMessage());
            }
        }
    }
    private void setupMediaPlayerListeners(MediaPlayer mediaPlayer, Button button, SeekBar seekBar) {
        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration());
            mp.start();
            button.setText("⏸️");
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            button.setText("▶️");
            seekBar.setProgress(0);
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            notifyFailure(null, "Ошибка медиаплеера: " + what);
            return true;
        });
    }
    private void setupSeekBarListener(MediaPlayer mediaPlayer, SeekBar seekBar) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    private void startProgressUpdater(MediaPlayer mediaPlayer, SeekBar seekBar) {
        new Thread(() -> {
            while (mediaPlayer != null) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        int currentPosition = mediaPlayer.getCurrentPosition();
                        activity.runOnUiThread(() -> seekBar.setProgress(currentPosition));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }
}