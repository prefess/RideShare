package com.example.rideshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.rideshare.R;

public class ConfirmationActivity extends AppCompatActivity {

    private TextView backToHomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        backToHomeTextView = findViewById(R.id.back_to_home_text);
        backToHomeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to home
                Intent intent = new Intent(ConfirmationActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}