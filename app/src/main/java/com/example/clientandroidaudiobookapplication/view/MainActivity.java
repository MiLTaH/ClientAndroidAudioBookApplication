package com.example.clientandroidaudiobookapplication.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.clientandroidaudiobookapplication.R;
import com.example.clientandroidaudiobookapplication.controller.FailMethod;
import com.example.clientandroidaudiobookapplication.controller.MainController;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.LoginRegResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    private Button authButton;
    private Button redirectRegButton;
    private EditText editLogin;
    private EditText editPassword;
    private String host;
    private GeneraAppContainer app;
    public EditText getEditLogin() {
        return editLogin;
    }
    public EditText getEditPassword() {
        return editPassword;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addListenerOnButton();
        Properties properties = new Properties();
        try (InputStream input = getAssets().open("application.properties")) {
            properties.load(input);
            host = properties.getProperty("requestURL");
            if (host == null || host.isEmpty()) {
                FailMethod.onFailure("URL-адрес запроса не задан в application.properties.", this);
                return;
            }
        } catch (FileNotFoundException e) {
            FailMethod.onFailure("Файл application.properties не найден. Убедитесь, что файл существует.", this);
        } catch (IOException e) {
            FailMethod.onFailure("Ошибка чтения файла application.properties: " + e.getMessage(), this);
        }
        app = (GeneraAppContainer) getApplication();
        app.setHost(host);

        editLogin = findViewById(R.id.editTextLogin);
        editPassword = findViewById(R.id.editTextPassword);
    }

    public void addListenerOnButton() {
        redirectRegButton = findViewById(R.id.redirectToRegButton);
        MainController mainController = new MainController(this);
        redirectRegButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        authButton = findViewById(R.id.authorizeButton);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainController.login(new MyCallback() {
                    @Override
                    public void onSuccess(Object token1) {
                        LoginRegResponse response = (LoginRegResponse) token1;
                        app.setToken(response.getToken());
                        Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        whenFailed(errorMessage);
                    }
                }, app);
            }
        });
    }
    public void whenFailed(String errorMessage) {
        FailMethod.onFailure(errorMessage, this);
    }
}