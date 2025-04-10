package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.controller.PopularBooksController;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class PopularActivity extends AppCompatActivity {
    private GeneraAppContainer app;
    private LinearLayout linearLayoutPopularBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_popular);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        app = (GeneraAppContainer) getApplication();
        linearLayoutPopularBooks = findViewById(R.id.linearLayoutPopularBooks);
        addListenerOnButton();
        loadPopularBooks();
    }

    private void loadPopularBooks() {
        PopularBooksController controller = new PopularBooksController(this);
        controller.getPopularBooks(new MyCallback<List<FindBooksResponse>>() {
            @Override
            public void onSuccess(List<FindBooksResponse> result) {
                linearLayoutPopularBooks.removeAllViews();
                for (FindBooksResponse item : result) {
                    LinearLayout itemLayout = new LinearLayout(PopularActivity.this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    View separatorTop = new View(PopularActivity.this);
                    separatorTop.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    separatorTop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    itemLayout.addView(separatorTop);
                    TextView bookNameTextView = new TextView(PopularActivity.this);
                    bookNameTextView.setText(item.getBookName());
                    bookNameTextView.setTextSize(22);
                    bookNameTextView.setClickable(true);
                    bookNameTextView.setFocusable(true);
                    bookNameTextView.setOnClickListener(v -> {
                        controller.showVoiceSelectionDialog(new MyCallback<List<ActorVoicesResponse>>() {
                            @Override
                            public void onSuccess(List<ActorVoicesResponse> result) {
                                showVoicesDialog(result, item);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                whenFailed(errorMessage);
                            }
                        }, item, app);
                    });
                    itemLayout.addView(bookNameTextView);
                    TextView authorNameTextView = new TextView(PopularActivity.this);
                    authorNameTextView.setText(item.getAuthorName());
                    authorNameTextView.setTextSize(10);
                    itemLayout.addView(authorNameTextView);
                    View separatorBottom = new View(PopularActivity.this);
                    separatorBottom.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1));
                    separatorBottom.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    itemLayout.addView(separatorBottom);
                    linearLayoutPopularBooks.addView(itemLayout);
                }
            }
            @Override
            public void onFailure(String errorMessage) {
                whenFailed(errorMessage);
            }
        }, app);
    }

    private void showVoicesDialog(List<ActorVoicesResponse> voices, FindBooksResponse book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PopularActivity.this);
        builder.setTitle("Выберите озвучку для " + book.getBookName());
        String[] voiceOptions = new String[voices.size()];
        int[] idVoices = new int[voices.size()];
        for (int i = 0; i < voices.size(); i++) {
            voiceOptions[i] = voices.get(i).getNameActor();
            idVoices[i] = voices.get(i).getId();
        }
        builder.setItems(voiceOptions, (dialog, which) -> {
            String selectedVoice = voiceOptions[which];
            int selectedID = idVoices[which];
            Intent intent = new Intent(PopularActivity.this, BookDetailsActivity.class);
            intent.putExtra("BOOK_NAME", book.getBookName());
            intent.putExtra("SELECTED_VOICE", selectedVoice);
            intent.putExtra("VOICE_ID", String.valueOf(selectedID));
            intent.putExtra("BOOK_ID", String.valueOf(book.getId()));
            intent.putExtra("AUTHOR", book.getAuthorName());
            startActivity(intent);
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void whenFailed(String errorMessage) {
        FailMethod.onFailure(errorMessage, this);
    }
    private void addListenerOnButton() {
        Button menuFromPopularButton = findViewById(R.id.menuFromPopularButton);
        menuFromPopularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopularActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
    }
}