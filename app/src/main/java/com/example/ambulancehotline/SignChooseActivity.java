package com.example.ambulancehotline;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SignChooseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_choose);

        getSupportActionBar().hide();
    }
}