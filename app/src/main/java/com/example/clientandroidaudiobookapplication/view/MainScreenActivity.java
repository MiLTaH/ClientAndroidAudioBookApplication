package com.example.clientandroidaudiobookapplication.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.controller.MainScreenController;
import com.example.clientandroidaudiobookapplication.models.ActorVoicesResponse;
import com.example.clientandroidaudiobookapplication.models.FindBooksResponse;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.LoginRegResponse;
import com.example.clientandroidaudiobookapplication.models.MyCallback;

import java.util.List;

public class MainScreenActivity extends AppCompatActivity {

    public EditText editText;
    private LinearLayout linearLayout;
    private GeneraAppContainer app;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addListenerOnButton();
        editText = findViewById(R.id.editTextText);
        app = (GeneraAppContainer) getApplication();
        linearLayout = findViewById(R.id.linearLayoutVerticalBooks);
    }
    public void addListenerOnButton() {
        Button redictStartButton = findViewById(R.id.RedictToStartButton);
        redictStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        MainScreenController mainScreenActivity = new MainScreenController(this);
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainScreenActivity.this, MenuActivity.class);
                startActivity(intent);
            }
        });
        Button findButton = findViewById(R.id.FindBooksButton);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.removeAllViews();
                mainScreenActivity.findBooks(new MyCallback<List<FindBooksResponse>>() {
                    @Override
                    public void onSuccess(List<FindBooksResponse> result) {
                        for (FindBooksResponse item : result) {
                            LinearLayout itemLayout = new LinearLayout(MainScreenActivity.this);
                            itemLayout.setOrientation(LinearLayout.VERTICAL);

                            View separatorTop = new View(MainScreenActivity.this);
                            separatorTop.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            separatorTop.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            itemLayout.addView(separatorTop);

                            TextView bookNameTextView = new TextView(MainScreenActivity.this);
                            bookNameTextView.setText(item.getBookName());
                            bookNameTextView.setTextSize(22);
                            bookNameTextView.setClickable(true);
                            bookNameTextView.setFocusable(true);

                            bookNameTextView.setOnClickListener(v -> {
                                mainScreenActivity.showVoiceSelectionDialog(new MyCallback<List<ActorVoicesResponse>>() {
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
                            TextView authorNameTextView = new TextView(MainScreenActivity.this);
                            authorNameTextView.setText(item.getAuthorName());
                            authorNameTextView.setTextSize(10);
                            itemLayout.addView(authorNameTextView);
                            View separatorBottom = new View(MainScreenActivity.this);
                            separatorBottom.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, 1));
                            separatorBottom.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                            itemLayout.addView(separatorBottom);
                            linearLayout.addView(itemLayout);
                        }
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        whenFailed(errorMessage);
                    }
                    private void showVoicesDialog(List<ActorVoicesResponse> voices, FindBooksResponse book) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainScreenActivity.this);
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
                            Intent intent = new Intent(MainScreenActivity.this, BookDetailsActivity.class);
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
                }, app);
            }
        });
    }
    private void whenFailed(String errorMessage){
        FailMethod.onFailure(errorMessage, this);
    }
}