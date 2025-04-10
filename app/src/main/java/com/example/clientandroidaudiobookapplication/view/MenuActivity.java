package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;

public class MenuActivity extends AppCompatActivity {
    private GeneraAppContainer app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        app = (GeneraAppContainer) getApplication();
        addListenerOnButton();
    }

    private void addListenerOnButton() {
        Button mainScreenActivity = findViewById(R.id.mainScreenActivity);
        mainScreenActivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, MainScreenActivity.class);
                startActivity(intent);
            }
        });
        Button popularButton = findViewById(R.id.popularButton);
        popularButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, PopularActivity.class);
                startActivity(intent);
            }
        });
    }
}