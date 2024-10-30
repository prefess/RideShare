package com.example.rideshare.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.rideshare.R;
import com.example.rideshare.adapter.ViewPagerAdapter;
import com.example.rideshare.fragment.OTPVerificationFragment;
import com.example.rideshare.fragment.PhoneInputFragment;
import com.example.rideshare.fragment.RegisterFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPagerAdapter adapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();


        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onSuccess: " + mAuth.getCurrentUser().getPhoneNumber());
            goToMainActivity();
        } else {
            setupViewPager();
        }

    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.view_pager_home);
        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Disable swipe navigation

        // Add the PhoneInputFragment to the adapter
        PhoneInputFragment phoneInputFragment = new PhoneInputFragment();
        adapter.addFragment(phoneInputFragment);
        viewPager.setCurrentItem(0);
    }

    public void goToRegisterFragment(String phoneNumber) {
        RegisterFragment registerFragment = new RegisterFragment(phoneNumber);
        adapter.addFragment(registerFragment);
        viewPager.setCurrentItem(adapter.getItemCount() - 1);
    }

    public void goToOTPVerificationFragment(OTPVerificationFragment otpFragment) {
//        OTPVerificationFragment otpVerificationFragment = new OTPVerificationFragment(verificationId, phoneNumber);
        adapter.addFragment(otpFragment);
        viewPager.setCurrentItem(adapter.getItemCount() - 1);
    }

    public void goToOTPVerificationFragment(String verificationId, String phoneNumber) {
        OTPVerificationFragment otpVerificationFragment = new OTPVerificationFragment(verificationId, phoneNumber);
        adapter.addFragment(otpVerificationFragment);
        viewPager.setCurrentItem(adapter.getItemCount() - 1);
    }

    public void goToMainActivity() {
        Intent intent = new Intent(Home.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}