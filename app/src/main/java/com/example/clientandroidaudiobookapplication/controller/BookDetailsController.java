package com.example.clientandroidaudiobookapplication.controller;

import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Color;

import com.bumptech.glide.Glide;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.BookDescriptionResponse;
import com.example.clientandroidaudiobookapplication.models.ChapterResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.view.BookDetailsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.widget.Toast;
import android.view.ContextThemeWrapper;


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
                              ActorVoicesResponse voice,
                              GeneraAppContainer app) {
        String url = String.format("%s/chapters/%d/%d", app.getHost(), book.getId(), voice.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<List<ChapterResponse>>() {}.getType(), callback);
    }

    public void getBookImage(MyCallback<BookDescriptionResponse> callback,
                             FindBooksResponse book,
                             GeneraAppContainer app) {
        String url = String.format("%s/books/%d/description", app.getHost(), book.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<BookDescriptionResponse>() {}.getType(), callback);
    }

    public void subscribeOrCancelBook(MyCallback<String> callback,
                                      String userName,
                                      String bookName,
                                      GeneraAppContainer app) {
        try {
            String json = String.format("{\"userName\":\"%s\",\"bookName\":\"%s\"}", userName, bookName);
            Request request = new Request.Builder()
                    .url(app.getHost() + "/user/subscribeUserToBook")
                    .addHeader("Authorization", "Bearer " + app.getToken())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            executeStringRequest(request, callback);
        } catch (Exception e) {
            notifyFailure(callback, "Ошибка при создании запроса: " + e.getMessage());
        }
    }

    public void updateChapters(List<ChapterResponse> chapters, LinearLayout layout) {
        activity.runOnUiThread(() -> {
            layout.removeAllViews();
            for (ChapterResponse chapter : chapters) {
                LinearLayout chapterRow = createChapterRow(chapter);
                layout.addView(chapterRow);
                setupChapterPlayer(chapter, chapterRow);
            }
        });
    }

    public void fetchLibriVoxLinks(MyCallback<List<ChapterResponse>> callback,
                                   FindBooksResponse book,
                                   GeneraAppContainer app) {
        String url = String.format("%s/librivox/%d/chapters", app.getHost(), book.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<List<ChapterResponse>>() {}.getType(), callback);
    }

    public void getLibriVoxDescription(MyCallback<BookDescriptionResponse> callback,
                                       FindBooksResponse book,
                                       GeneraAppContainer app) {
        String url = String.format("%s/librivox/book/description/%d", app.getHost(), book.getId());
        Request request = createAuthorizedRequest(url, app.getToken());
        executeRequest(request, new TypeToken<BookDescriptionResponse>() {}.getType(), callback);
    }

    private LinearLayout createChapterRow(ChapterResponse chapter) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 16, 0, 16);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView title = new TextView(activity);
        title.setText(chapter.getNameChapter());
        title.setTextSize(16);
        title.setPadding(16, 8, 16, 8);

        LinearLayout controls = new LinearLayout(activity);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setPadding(16, 8, 16, 8);

        // Кнопка воспроизведения
        Button playPause = new Button(activity);
        playPause.setText("▶️");
        playPause.setPadding(16, 8, 16, 8);
        playPause.setBackgroundColor(Color.LTGRAY);
        playPause.setAllCaps(false);

        // Обычный SeekBar без стиля Material
        SeekBar seekBar = new SeekBar(new ContextThemeWrapper(activity, android.R.style.Widget_SeekBar));
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        seekBar.setMax(100);
        seekBar.setProgress(0);

        controls.addView(playPause);
        controls.addView(seekBar);

        row.addView(title);
        row.addView(controls);

        return row;
    }
    private void setupChapterPlayer(ChapterResponse chapter, LinearLayout chapterRow) {
        LinearLayout controlsLayout = (LinearLayout) chapterRow.getChildAt(1);
        Button playPauseButton = (Button) controlsLayout.getChildAt(0);
        SeekBar seekBar = (SeekBar) controlsLayout.getChildAt(1);

        MediaPlayer mediaPlayer = new MediaPlayer();

        playPauseButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setText("▶️");
            } else {
                try {
                    if (mediaPlayer.getCurrentPosition() > 0) {
                        mediaPlayer.start();
                        playPauseButton.setText("⏸️");
                    } else {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(chapter.getChapterUrl());
                        mediaPlayer.setOnPreparedListener(mp -> {
                            seekBar.setMax(mp.getDuration());
                            mp.start();
                            playPauseButton.setText("⏸️");
                            startProgressUpdater(mediaPlayer, seekBar);
                        });
                        mediaPlayer.setOnCompletionListener(mp -> {
                            playPauseButton.setText("▶️");
                            seekBar.setProgress(0);
                        });
                        mediaPlayer.prepareAsync();
                    }
                } catch (IOException e) {
                    Toast.makeText(activity, "Ошибка воспроизведения: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
                        int position = mediaPlayer.getCurrentPosition();
                        activity.runOnUiThread(() -> seekBar.setProgress(position));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {
                    break;
                }
            }
        }).start();
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
            @Override public void onFailure(Call call, IOException e) {
                notifyFailure(callback, "Ошибка запроса: " + e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    notifyFailure(callback, "Пустой ответ от сервера.");
                    return;
                }
                String body = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        T result = gson.fromJson(body, type);
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
            @Override public void onFailure(Call call, IOException e) {
                notifyFailure(callback, "Ошибка запроса: " + e.getMessage());
            }
            @Override public void onResponse(Call call, Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    notifySuccess(callback, body);
                } else {
                    notifyFailure(callback, "Ошибка сервера: " + response.code());
                }
            }
        });
    }

    private <T> void notifySuccess(MyCallback<T> callback, T result) {
        if (callback != null)
            activity.runOnUiThread(() -> callback.onSuccess(result));
    }

    private <T> void notifyFailure(MyCallback<T> callback, String error) {
        if (callback != null)
            activity.runOnUiThread(() -> callback.onFailure(error));
    }
}