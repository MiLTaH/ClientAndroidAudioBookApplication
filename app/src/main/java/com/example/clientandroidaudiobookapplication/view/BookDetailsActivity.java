package com.example.clientandroidaudiobookapplication.view;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.BookDetailsController;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.BookDescriptionResponse;
import com.example.clientandroidaudiobookapplication.models.ChapterResponse;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class BookDetailsActivity extends AppCompatActivity {
    private LinearLayout chaptersLayout;
    private GeneraAppContainer app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        chaptersLayout = findViewById(R.id.LinearLayoutChapters);
        TextView bookNameInput = findViewById(R.id.textView11);
        TextView authorInput = findViewById(R.id.textView12);
        TextView voiceActorInput = findViewById(R.id.textView10);

        app = (GeneraAppContainer) getApplication();
        ActorVoicesResponse actorVoice = gettingFromPreviousPageActorVoice();
        FindBooksResponse booksResponse = gettingFromPreviousPageBook();
        bookNameInput.setText(booksResponse.getBookName());
        authorInput.setText(booksResponse.getAuthorName());
        voiceActorInput.setText(actorVoice.getNameActor());
        BookDetailsController bookDetailsController = new BookDetailsController(this);

        bookDetailsController.fetchChapters(new MyCallback<List<ChapterResponse>>() {
            @Override
            public void onSuccess(List<ChapterResponse> result) {
                bookDetailsController.updateChapters(result, chaptersLayout);
            }

            @Override
            public void onFailure(String errorMessage) {
                FailMethod.onFailure(errorMessage, BookDetailsActivity.this);
            }
        }, booksResponse, actorVoice, app);
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
        }, booksResponse, app);
    }
    private ActorVoicesResponse gettingFromPreviousPageActorVoice() {
        int id = Integer.parseInt(getIntent().getStringExtra("VOICE_ID"));
        String nameActor = getIntent().getStringExtra("SELECTED_VOICE");
        return new ActorVoicesResponse(id,nameActor);
    }
    private FindBooksResponse gettingFromPreviousPageBook() {
        int id = Integer.parseInt(getIntent().getStringExtra("BOOK_ID"));
        String bookName = getIntent().getStringExtra("BOOK_NAME");
        String authorName = getIntent().getStringExtra("AUTHOR");
        return new FindBooksResponse(id,bookName,authorName);
    }
}