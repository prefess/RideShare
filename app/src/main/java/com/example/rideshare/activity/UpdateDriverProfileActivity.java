package com.example.rideshare.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UpdateDriverProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PROFILE = 1;
    private static final int REQUEST_GALLERY_PROFILE = 2;
    private static final int REQUEST_CAMERA_LICENSE = 3;
    private static final int REQUEST_GALLERY_LICENSE = 4;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private EditText usernameEditText, emailEditText, mobileEditText;
    private ImageView profileImageView, updateProfileImage, updateLicenseImage,driverLicenseImageView;
    private Button updateButton;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Uri profileImageUri;
    private Uri licenseImageUri;
    private StorageReference storageReference;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_driver_profile);
        // Initialize views
        profileImageView = findViewById(R.id.image_profile);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email_address);
        mobileEditText = findViewById(R.id.mobile_phone);
        updateButton = findViewById(R.id.publish_ride_button);
        updateProfileImage = findViewById(R.id.update_profile_image);
        updateLicenseImage = findViewById(R.id.update_license_image);
        driverLicenseImageView = findViewById(R.id.driverLicenseImageView);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(userId);
        }

        // Fetch user data from Firebase
        fetchUserData();

        // Set onClickListener to open image picker
        updateProfileImage.setOnClickListener(v -> showImagePickerOptions(REQUEST_GALLERY_PROFILE, REQUEST_CAMERA_PROFILE));
        updateLicenseImage.setOnClickListener(v -> showImagePickerOptions(REQUEST_GALLERY_LICENSE, REQUEST_CAMERA_LICENSE));

        // Set update button click listener
        updateButton.setOnClickListener(v -> updateUserData());

    }

    // Fetch user data from Firebase
    private void fetchUserData() {
        if (currentUser != null) {
            userDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String username = dataSnapshot.child("name").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String mobile = dataSnapshot.child("phoneNumber").getValue(String.class);
                        String profileImageUrl = dataSnapshot.child("profilePicture").getValue(String.class);
                        String driverLicensePictureUrl = dataSnapshot.child("driverLicensePicture").getValue(String.class);
                        // Bind data to EditText fields
                        if (username != null) {
                            usernameEditText.setText(username);
                        }
                        if (email != null) {
                            emailEditText.setText(email);
                        }
                        if (mobile != null) {
                            mobileEditText.setText(mobile);
                        }

                        // Load profile image if it exists
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(UpdateDriverProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_profile) // Placeholder image
                                    .circleCrop() // This will make the image circular
                                    .into(profileImageView);
                        } else {
                            // Set default image if no profile image exists
                            profileImageView.setImageResource(R.drawable.ic_profile);
                        }

                        Glide.with(UpdateDriverProfileActivity.this).load(driverLicensePictureUrl).into(driverLicenseImageView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UpdateDriverProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY_PROFILE || requestCode == REQUEST_GALLERY_LICENSE) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (requestCode == REQUEST_GALLERY_PROFILE) {
                        profileImageUri = selectedImageUri;
                        profileImageView.setImageURI(profileImageUri);
                    } else if (requestCode == REQUEST_GALLERY_LICENSE) {
                        licenseImageUri = selectedImageUri;
                        driverLicenseImageView.setImageURI(licenseImageUri);
                    }
                }
            } else if (requestCode == REQUEST_CAMERA_PROFILE) {
                profileImageView.setImageURI(profileImageUri);
            } else if (requestCode == REQUEST_CAMERA_LICENSE) {
                driverLicenseImageView.setImageURI(licenseImageUri);
            }
        }
    }

    private void updateUserData() {
        dialog = new Dialog(UpdateDriverProfileActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
        // Update user data in Firebase
        String updatedUsername = usernameEditText.getText().toString().trim();
        String updatedEmail = emailEditText.getText().toString().trim();
        String updatedMobile = mobileEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(updatedUsername) && !TextUtils.isEmpty(updatedEmail) && !TextUtils.isEmpty(updatedMobile)) {
            userDatabaseReference.child("name").setValue(updatedUsername);
            userDatabaseReference.child("email").setValue(updatedEmail);
            userDatabaseReference.child("phoneNumber").setValue(updatedMobile);

            if (profileImageUri != null) {
                uploadProfileImageToFirebase(profileImageUri);
            }
            if (licenseImageUri != null) {
                uploadLicenseImageToFirebase(licenseImageUri);
            }

        } else {
            dialog.dismiss();
            Toast.makeText(UpdateDriverProfileActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImageToFirebase(Uri uri) {
        StorageReference fileReference = storageReference.child("drivers/" + currentUser.getUid() + "/profile.jpg");
        fileReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    userDatabaseReference.child("profilePicture").setValue(uri1.toString());
                    Toast.makeText(UpdateDriverProfileActivity.this, "Profile picture updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }))
                .addOnFailureListener(e -> Toast.makeText(UpdateDriverProfileActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show());
    }

    private void uploadLicenseImageToFirebase(Uri uri) {
        StorageReference fileReference = storageReference.child("drivers/" + currentUser.getUid() + "/license.jpg");
        fileReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    userDatabaseReference.child("driverLicensePicture").setValue(uri1.toString());
                    Toast.makeText(UpdateDriverProfileActivity.this, "License picture updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }))
                .addOnFailureListener(e -> Toast.makeText(UpdateDriverProfileActivity.this, "Failed to upload license picture", Toast.LENGTH_SHORT).show());
    }

    private void showImagePickerOptions(int requestGalleryCode, int requestCameraCode) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(UpdateDriverProfileActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(UpdateDriverProfileActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateDriverProfileActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera(requestCameraCode);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                if (ContextCompat.checkSelfPermission(UpdateDriverProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(UpdateDriverProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    openGallery(requestGalleryCode);
                }
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    private void openCamera(int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = AndroidUtil.createImageFile(this);
            } catch (IOException ex) {
                Log.e("DriverRegisterActivity", "Error occurred while creating the File", ex);
            }
            if (photoFile != null) {
                Uri imageUri = FileProvider.getUriForFile(this, "com.example.rideshare.fileprovider", photoFile);
                if (requestCode == REQUEST_CAMERA_PROFILE) {
                    profileImageUri = imageUri;
                } else if (requestCode == REQUEST_CAMERA_LICENSE) {
                    licenseImageUri = imageUri;
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(requestCode);
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery(requestCode);
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}