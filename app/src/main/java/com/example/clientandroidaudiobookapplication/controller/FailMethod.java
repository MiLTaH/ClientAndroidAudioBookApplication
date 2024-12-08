package com.example.clientandroidaudiobookapplication.controller;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class FailMethod {
    public static void onFailure(String errorMessage, Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Ошибка")
                .setMessage(errorMessage)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
