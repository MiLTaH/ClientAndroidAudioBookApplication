package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.BookDetailsController;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.BookDescriptionResponse;
import com.example.clientandroidaudiobookapplication.models.ChapterResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {

    private LinearLayout chaptersLayout;
    private Button readOrCancelButton;
    private GeneraAppContainer app;
    private BookDetailsController bookDetailsController;
    private FindBooksResponse currentBook;
    private ActorVoicesResponse currentVoice;
    private boolean isFromLibriVox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);

        setupWindowInsets();
        initializeViews();
        initializeAppContainer();
        extractIntentData();
        initializeController();
        displayBasicBookInfo();
        loadBookContent();
        configureButtons();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        chaptersLayout = findViewById(R.id.LinearLayoutChapters);
        readOrCancelButton = findViewById(R.id.readOrCancelButton);
    }

    private void initializeAppContainer() {
        app = (GeneraAppContainer) getApplication();
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        isFromLibriVox = intent.getBooleanExtra("IS_LIBRIVOX", false);
        currentBook = new FindBooksResponse(
                intent.getIntExtra("BOOK_ID", 0),
                intent.getStringExtra("BOOK_NAME"),
                intent.getStringExtra("AUTHOR")
        );
        currentVoice = isFromLibriVox ?
                new ActorVoicesResponse(-1, "LibriVox") :
                new ActorVoicesResponse(
                        Integer.parseInt(intent.getStringExtra("VOICE_ID")),
                        intent.getStringExtra("SELECTED_VOICE")
                );
    }

    private void initializeController() {
        bookDetailsController = new BookDetailsController(this);
    }

    private void displayBasicBookInfo() {
        ((TextView) findViewById(R.id.textView11)).setText(currentBook.getBookName());
        ((TextView) findViewById(R.id.textView12)).setText(currentBook.getAuthorName());
        ((TextView) findViewById(R.id.textView10)).setText(currentVoice.getNameActor());
    }

    private void loadBookContent() {
        if (isFromLibriVox) {
            loadLibriVoxChapters();
            loadDescriptionAndImage(true);
        } else {
            loadStandardChapters();
            loadDescriptionAndImage(false);
        }
    }

    private void loadStandardChapters() {
        bookDetailsController.fetchChapters(new MyCallback<List<ChapterResponse>>() {
            @Override
            public void onSuccess(List<ChapterResponse> result) {
                bookDetailsController.updateChapters(result, chaptersLayout);
            }

            @Override
            public void onFailure(String errorMessage) {
                FailMethod.onFailure(errorMessage, BookDetailsActivity.this);
            }
        }, currentBook, currentVoice, app);
    }

    private void loadLibriVoxChapters() {
        bookDetailsController.fetchLibriVoxLinks(new MyCallback<List<ChapterResponse>>() {
            @Override
            public void onSuccess(List<ChapterResponse> result) {
                bookDetailsController.updateChapters(result, chaptersLayout);
            }

            @Override
            public void onFailure(String errorMessage) {
                FailMethod.onFailure(errorMessage, BookDetailsActivity.this);
            }
        }, currentBook, app);
    }

    private void loadDescriptionAndImage(boolean isLibriVox) {
        MyCallback<BookDescriptionResponse> callback = new MyCallback<BookDescriptionResponse>() {
            @Override
            public void onSuccess(BookDescriptionResponse result) {
                TextView descriptionView = findViewById(R.id.textView9);
                ImageView imageView = findViewById(R.id.imageView);
                descriptionView.setText(result.getDescription());
                bookDetailsController.loadImage(result.getBookImageUrl(), imageView);
            }

            @Override
            public void onFailure(String errorMessage) {
                FailMethod.onFailure(errorMessage, BookDetailsActivity.this);
            }
        };

        if (isLibriVox) {
            bookDetailsController.getLibriVoxDescription(callback, currentBook, app);
        } else {
            bookDetailsController.getBookImage(callback, currentBook, app);
        }
    }

    private void configureButtons() {
        Button recommendButton = findViewById(R.id.recommend);
        recommendButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RecommendedActivity.class);
            intent.putExtra("BOOK_NAME", currentBook.getBookName());
            startActivity(intent);
        });

        readOrCancelButton.setText("Читать");
        readOrCancelButton.setOnClickListener(v -> handleBookSubscription());
    }

    private void handleBookSubscription() {
        bookDetailsController.subscribeOrCancelBook(new MyCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    boolean isSubscribed = "attached".equals(result);
                    readOrCancelButton.setText(isSubscribed ? "Отписаться" : "Читать");
                    Toast.makeText(BookDetailsActivity.this,
                            isSubscribed ? "Вы подписались на книгу" : "Вы отписались от книги",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(BookDetailsActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
            }
        }, app.getUsername(), currentBook.getBookName(), app);
    }
}
