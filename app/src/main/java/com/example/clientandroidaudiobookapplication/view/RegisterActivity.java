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
import com.example.clientandroidaudiobookapplication.controller.RegisterController;
import com.example.clientandroidaudiobookapplication.models.GeneraAppContainer;
import com.example.clientandroidaudiobookapplication.models.MyCallback;
import com.example.clientandroidaudiobookapplication.models.LoginRegResponse;

public class RegisterActivity extends AppCompatActivity {
    private EditText editLogin;
    private EditText editEmailAddress;
    private EditText editPassword;
    private GeneraAppContainer app;
    public EditText getEditLogin() {
        return editLogin;
    }
    public EditText getEditEmailAddress() {
        return editEmailAddress;
    }
    public EditText getEditPassword() {
        return editPassword;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (GeneraAppContainer) getApplication();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addListenerOnButton();
        editLogin = findViewById(R.id.editTextLoginReg);
        editEmailAddress = findViewById(R.id.editTextEmailAddressReg);
        editPassword =  findViewById(R.id.editTextPasswordReg);
    }

    public void addListenerOnButton() {
        Button redirectAuthorizeButton = findViewById(R.id.RedirectAuthButton);
        redirectAuthorizeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        RegisterController registerController = new RegisterController(this);
        Button regButton = findViewById(R.id.RegistrationButton);
        regButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                registerController.register(new MyCallback() {
                    @Override
                    public void onSuccess(Object token1) {
                        LoginRegResponse response = (LoginRegResponse) token1;
                        app.setToken(response.getToken());
                        app.setUsername(editLogin.toString());
                        Intent intent = new Intent(RegisterActivity.this, MainScreenActivity.class);
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
    private void whenFailed(String errorMessage) {
        FailMethod.onFailure(errorMessage,this);
    }
}