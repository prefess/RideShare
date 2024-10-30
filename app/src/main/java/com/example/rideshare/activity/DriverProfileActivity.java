package com.example.rideshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverProfileActivity extends AppCompatActivity {

    private TextView nameTextView, emailTextView, phoneNumberTextView, verifiedTextView, ratingTextView;
    private ImageView profileImageView, driverLicenseImageView, editProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private boolean verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        editProfile = findViewById(R.id.edit_profile);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (verified) {
                    AndroidUtil.showToast(DriverProfileActivity.this, "Your profile has been verified");
                } else {
                    Intent intent = new Intent(DriverProfileActivity.this, UpdateDriverProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);
        verifiedTextView = findViewById(R.id.verifiedTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        profileImageView = findViewById(R.id.profileImageView);
        driverLicenseImageView = findViewById(R.id.driverLicenseImageView);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadDriverProfile();
    }

    private void loadDriverProfile() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child("Drivers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    verified = snapshot.child("verified").getValue(Boolean.class);
                    float rating = snapshot.child("totalRatingScore").getValue(Float.class);
                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);
                    String driverLicensePictureUrl = snapshot.child("driverLicensePicture").getValue(String.class);

                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    phoneNumberTextView.setText(phoneNumber);
                    verifiedTextView.setText(verified ? "Verified" : "Not Verified");
                    ratingTextView.setText(String.valueOf(rating));

                    Glide.with(DriverProfileActivity.this).load(profilePictureUrl).into(profileImageView);
                    Glide.with(DriverProfileActivity.this).load(driverLicensePictureUrl).into(driverLicenseImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }
}