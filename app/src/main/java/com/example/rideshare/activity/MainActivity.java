package com.example.rideshare.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rideshare.R;
import com.example.rideshare.fragment.BookARideFragment;
import com.example.rideshare.fragment.ProfileFragment;
import com.example.rideshare.fragment.PublishFragment;
import com.example.rideshare.fragment.YourRidesFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new BookARideFragment()).commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_book_a_ride) {
                selectedFragment = new BookARideFragment();
            } else if (itemId == R.id.navigation_publish) {
                selectedFragment = new PublishFragment();
            } else if (itemId == R.id.navigation_your_rides) {
                selectedFragment = new YourRidesFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }

            return true;
        });
    }
}