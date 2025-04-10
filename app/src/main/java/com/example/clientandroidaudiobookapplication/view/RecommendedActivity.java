package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.controller.RecommendedController;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class RecommendedActivity extends AppCompatActivity {
    private GeneraAppContainer app;
    private LinearLayout linearLayoutBooks;
    private RecommendedController controller;
    private String currentBookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recomended);

        app = (GeneraAppContainer) getApplication();
        linearLayoutBooks = findViewById(R.id.booksLayout);
        currentBookName = getIntent().getStringExtra("BOOK_NAME");

        addListenerOnButton();
        loadRecommendedBooks();
    }

    private void loadRecommendedBooks() {
        linearLayoutBooks.removeAllViews();
        controller = new RecommendedController(this);
        controller.getBookGenres(currentBookName, new MyCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> genres) {
                controller.getRecommendedBooks(genres, new MyCallback<List<FindBooksResponse>>() {
                    @Override
                    public void onSuccess(List<FindBooksResponse> books) {
                        showBooksList(books);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        whenFailed(errorMessage);
                    }
                }, app);
            }
            @Override
            public void onFailure(String errorMessage) {
                whenFailed(errorMessage);
            }
        }, app);
    }
    private void showBooksList(List<FindBooksResponse> books) {
        runOnUiThread(() -> {
            linearLayoutBooks.removeAllViews();

            if (books.isEmpty()) {
                TextView emptyView = new TextView(RecommendedActivity.this);
                emptyView.setText("Нет рекомендуемых книг");
                linearLayoutBooks.addView(emptyView);
                return;
            }

            for (FindBooksResponse book : books) {
                LinearLayout itemLayout = new LinearLayout(RecommendedActivity.this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                View separatorTop = new View(RecommendedActivity.this);
                separatorTop.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                separatorTop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                itemLayout.addView(separatorTop);
                TextView bookNameTextView = new TextView(RecommendedActivity.this);
                bookNameTextView.setText(book.getBookName());
                bookNameTextView.setTextSize(22);
                bookNameTextView.setClickable(true);
                bookNameTextView.setFocusable(true);
                bookNameTextView.setOnClickListener(v -> showVoiceDialog(book));
                itemLayout.addView(bookNameTextView);
                TextView authorNameTextView = new TextView(RecommendedActivity.this);
                authorNameTextView.setText(book.getAuthorName());
                authorNameTextView.setTextSize(10);
                itemLayout.addView(authorNameTextView);
                View separatorBottom = new View(RecommendedActivity.this);
                separatorBottom.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                separatorBottom.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                itemLayout.addView(separatorBottom);
                linearLayoutBooks.addView(itemLayout);
            }
        });
    }

    private void showVoiceDialog(FindBooksResponse book) {
        controller.showVoiceSelectionDialog(new MyCallback<List<ActorVoicesResponse>>() {
            @Override
            public void onSuccess(List<ActorVoicesResponse> voices) {
                showVoicesSelectionDialog(voices, book);
            }
            @Override
            public void onFailure(String errorMessage) {
                whenFailed(errorMessage);
            }
        }, book, app);
    }

    private void showVoicesSelectionDialog(List<ActorVoicesResponse> voices, FindBooksResponse book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Выберите озвучку для " + book.getBookName());

        String[] voiceNames = new String[voices.size()];
        int[] voiceIds = new int[voices.size()];

        for (int i = 0; i < voices.size(); i++) {
            voiceNames[i] = voices.get(i).getNameActor();
            voiceIds[i] = voices.get(i).getId();
        }

        builder.setItems(voiceNames, (dialog, which) -> {
            Intent intent = new Intent(RecommendedActivity.this, BookDetailsActivity.class);
            intent.putExtra("BOOK_NAME", book.getBookName());
            intent.putExtra("SELECTED_VOICE", voiceNames[which]);
            intent.putExtra("VOICE_ID", String.valueOf(voiceIds[which]));
            intent.putExtra("BOOK_ID", String.valueOf(book.getId()));
            intent.putExtra("AUTHOR", book.getAuthorName());
            startActivity(intent);
        });

        builder.setNegativeButton("Отмена", null);
        builder.show();
    }

    private void whenFailed(String errorMessage) {
        FailMethod.onFailure(errorMessage, this);
    }

    private void addListenerOnButton() {
        Button menuButton = findViewById(R.id.menuFromRecomendedButton);
        menuButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecommendedActivity.this, MenuActivity.class);
            startActivity(intent);
        });
    }
}