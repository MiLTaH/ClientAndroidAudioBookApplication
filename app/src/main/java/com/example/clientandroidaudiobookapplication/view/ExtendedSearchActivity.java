package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.ExtendedSearchController;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.controller.MainScreenController;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class ExtendedSearchActivity extends AppCompatActivity {
    public EditText editText;
    private LinearLayout linearLayout;
    private GeneraAppContainer app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extended_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editText = findViewById(R.id.editTextText);
        linearLayout = findViewById(R.id.linearLayoutVerticalBooks);
        app = (GeneraAppContainer) getApplication();

        addListenerOnButton();
    }

    public void addListenerOnButton() {
        ExtendedSearchController controller = new ExtendedSearchController(this);

        findViewById(R.id.menuButton).setOnClickListener(v ->
                startActivity(new Intent(ExtendedSearchActivity.this, MenuActivity.class)));

        findViewById(R.id.FindBooksButton).setOnClickListener(v -> {
            linearLayout.removeAllViews();
            controller.findBooksByTitle(new MyCallback<List<FindBooksResponse>>() {
                @Override
                public void onSuccess(List<FindBooksResponse> result) {

                    for (FindBooksResponse item : result) {
                        Log.d("BOOK_CHECK", "ID: " + item.getId() +
                                ", Name: " + item.getBookName() +
                                ", Author: " + item.getAuthorName());

                        LinearLayout itemLayout = new LinearLayout(ExtendedSearchActivity.this);
                        itemLayout.setOrientation(LinearLayout.VERTICAL);

                        View separatorTop = new View(ExtendedSearchActivity.this);
                        separatorTop.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        separatorTop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        itemLayout.addView(separatorTop);

                        TextView bookNameTextView = new TextView(ExtendedSearchActivity.this);
                        bookNameTextView.setText(item.getBookName());
                        bookNameTextView.setTextSize(22);
                        bookNameTextView.setClickable(true);
                        bookNameTextView.setOnClickListener(view -> {
                            Intent intent = new Intent(ExtendedSearchActivity.this, BookDetailsActivity.class);
                            intent.putExtra("BOOK_NAME", item.getBookName());
                            intent.putExtra("AUTHOR", item.getAuthorName());
                            intent.putExtra("BOOK_ID", item.getId());
                            intent.putExtra("IS_LIBRIVOX", true);
                            startActivity(intent);
                        });

                        itemLayout.addView(bookNameTextView);
                        TextView authorNameTextView = new TextView(ExtendedSearchActivity.this);
                        authorNameTextView.setText(item.getAuthorName());
                        authorNameTextView.setTextSize(10);
                        itemLayout.addView(authorNameTextView);
                        View separatorBottom = new View(ExtendedSearchActivity.this);
                        separatorBottom.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        separatorBottom.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        itemLayout.addView(separatorBottom);
                        linearLayout.addView(itemLayout);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    FailMethod.onFailure(errorMessage, ExtendedSearchActivity.this);
                }
            }, app);
        });
    }

    public String getSearchInput() {
        return editText.getText().toString().trim();
    }
}
