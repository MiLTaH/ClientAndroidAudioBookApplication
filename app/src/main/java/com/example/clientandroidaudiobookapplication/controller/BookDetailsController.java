package com.example.clientandroidaudiobookapplication.controller;

import android.media.MediaPlayer;
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
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.view.BookDetailsActivity;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class BookDetailsController {
    private BookDetailsActivity activity;

    public BookDetailsController(BookDetailsActivity activity) {
        this.activity = activity;
    }

    public void loadImage(String imageUrl, ImageView imageView) {
        Glide.with(activity)
                .load(imageUrl)
                .override(1024, 1024)
                .into(imageView);
    }
    public void fetchChapters(MyCallback<List<ChapterResponse>> callback, FindBooksResponse book, ActorVoicesResponse actorVoice, GeneraAppContainer app) {
        Request request = new Request.Builder()
                .url(app.getHost() + "/chapters" + "/" + book.getId() + "/" + actorVoice.getId())
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
                        Type listType = new TypeToken<List<ChapterResponse>>() {}.getType();
                        List<ChapterResponse> chapters = new Gson().fromJson(responseBody, listType);
                        activity.runOnUiThread(() -> callback.onSuccess(chapters));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> callback.onFailure("Ошибка обработки данных: " + e.getMessage()));
                    }
                } else {
                    activity.runOnUiThread(() -> callback.onFailure("Ошибка сервера: " + response.code()));
                }
            }
        });
    }
    public void getBookImage(MyCallback<BookDescriptionResponse> callback, FindBooksResponse book, GeneraAppContainer app) {
        Request request = new Request.Builder()
                .url(app.getHost() + "/books" +  "/" + book.getId() +"/description")
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
                        Type type = new TypeToken<BookDescriptionResponse>() {}.getType();
                        BookDescriptionResponse bookImage = new Gson().fromJson(responseBody, type);
                        activity.runOnUiThread(() -> callback.onSuccess(bookImage));
                    } catch (Exception e) {
                        activity.runOnUiThread(() -> callback.onFailure("Ошибка обработки данных: " + e.getMessage()));
                    }
                } else {
                    activity.runOnUiThread(() -> callback.onFailure("Ошибка сервера: " + response.code()));
                }
            }
        });
    }
    public void updateChapters(List<ChapterResponse> chapters, LinearLayout chaptersLayout) {
        chaptersLayout.removeAllViews();
        for (ChapterResponse chapter : chapters) {
            LinearLayout chapterRow = new LinearLayout(activity);
            chapterRow.setOrientation(LinearLayout.VERTICAL);

            TextView chapterTitle = new TextView(activity);
            chapterTitle.setText(chapter.getNameChapter());
            chapterTitle.setTextSize(12);
            chapterTitle.setPadding(8, 8, 8, 4);

            Button playPauseButton = new Button(activity);
            playPauseButton.setText("▶️");
            playPauseButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            SeekBar seekBar = new SeekBar(activity);
            seekBar.setMax(100);

            chapterRow.addView(chapterTitle);
            chapterRow.addView(playPauseButton);
            chapterRow.addView(seekBar);
            chaptersLayout.addView(chapterRow);

            MediaPlayer mediaPlayer = new MediaPlayer();
            playPauseButton.setOnClickListener(v -> {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playPauseButton.setText("▶️");
                } else {
                    try {
                        if (mediaPlayer.getCurrentPosition() == 0) {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(chapter.getChapterUrl());
                            mediaPlayer.prepare();
                        }
                        mediaPlayer.start();
                        playPauseButton.setText("⏸️");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            mediaPlayer.setOnPreparedListener(mp -> seekBar.setMax(mp.getDuration()));
            mediaPlayer.setOnCompletionListener(mp -> playPauseButton.setText("▶️"));
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            new Thread(() -> {
                while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
