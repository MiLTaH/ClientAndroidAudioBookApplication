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
    private GeneraAppContainer app;
    private BookDetailsController bookDetailsController;
    private FindBooksResponse currentBook;
    private ActorVoicesResponse currentVoice;
    private Button readOrCancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        setupWindowInsets();
        initializeViews();
        initializeAppContainer();
        getIntentData();
        setupControllers();
        setupUI();
        loadBookData();
        setupButtons();
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
    private void getIntentData() {
        currentVoice = getActorVoiceFromIntent();
        currentBook = getBookFromIntent();
    }
    private void setupControllers() {
        bookDetailsController = new BookDetailsController(this);
    }
    private void setupUI() {
        TextView bookNameInput = findViewById(R.id.textView11);
        TextView authorInput = findViewById(R.id.textView12);
        TextView voiceActorInput = findViewById(R.id.textView10);
        bookNameInput.setText(currentBook.getBookName());
        authorInput.setText(currentBook.getAuthorName());
        voiceActorInput.setText(currentVoice.getNameActor());
    }
    private void loadBookData() {
        loadChapters();
        loadBookDescriptionAndImage();
    }
    private void loadChapters() {
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

    private void loadBookDescriptionAndImage() {
        bookDetailsController.getBookImage(new MyCallback<BookDescriptionResponse>() {
            @Override
            public void onSuccess(BookDescriptionResponse result) {
                TextView bookDescription = findViewById(R.id.textView9);
                ImageView imageView = findViewById(R.id.imageView);
                bookDescription.setText(result.getDescription());
                bookDetailsController.loadImage(result.getBookImageUrl(), imageView);
            }
            @Override
            public void onFailure(String errorMessage) {
                FailMethod.onFailure(errorMessage, BookDetailsActivity.this);
            }
        }, currentBook, app);
    }

    private void setupButtons() {
        Button recommendButton = findViewById(R.id.recommend);
        recommendButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookDetailsActivity.this, RecommendedActivity.class);
            intent.putExtra("BOOK_NAME", currentBook.getBookName());
            startActivity(intent);
        });
        readOrCancelButton.setText("Читать");
        readOrCancelButton.setOnClickListener(v -> handleSubscription());
    }

    private void handleSubscription() {
        bookDetailsController.subscribeOrCancelBook(new MyCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    if ("attached".equals(result)) {
                        readOrCancelButton.setText("Отписаться");
                        Toast.makeText(BookDetailsActivity.this,
                                "Вы подписались на книгу", Toast.LENGTH_SHORT).show();
                    } else if ("detached".equals(result)) {
                        readOrCancelButton.setText("Читать");
                        Toast.makeText(BookDetailsActivity.this,
                                "Вы отписались от книги", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(BookDetailsActivity.this,
                                errorMessage, Toast.LENGTH_SHORT).show());
            }
        }, app.getUsername(), currentBook.getBookName(), app);
    }
    private ActorVoicesResponse getActorVoiceFromIntent() {
        int id = Integer.parseInt(getIntent().getStringExtra("VOICE_ID"));
        String nameActor = getIntent().getStringExtra("SELECTED_VOICE");
        return new ActorVoicesResponse(id, nameActor);
    }
    private FindBooksResponse getBookFromIntent() {
        int id = Integer.parseInt(getIntent().getStringExtra("BOOK_ID"));
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        String authorName = getIntent().getStringExtra("AUTHOR");
        return new FindBooksResponse(id, bookName, authorName);
    }
}