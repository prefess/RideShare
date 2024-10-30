package com.example.rideshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rideshare.R;

public class SlashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slash_screen);

        // Delay for 2 seconds before navigating to the main activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SlashScreenActivity.this, Home.class);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3000 milliseconds = 3 seconds
    }
}