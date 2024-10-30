package com.example.rideshare.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;


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


public class DriverRegisterActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK_PROFILE = 1;
    private static final int REQUEST_IMAGE_PICK_LICENSE = 2;
    private static final int REQUEST_IMAGE_CAMERA_PROFILE = 3;
    private static final int REQUEST_IMAGE_CAMERA_LICENSE = 4;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;

    private ImageView profileImageView;
    private ImageView licenseImageView;
    private Uri profileImageUri;
    private Uri licenseImageUri;

    private TextView nameInput;
    private TextView emailInput;

    private TextView phoneInput;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private String name, email, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_register);

        profileImageView = findViewById(R.id.profile_image);
        licenseImageView = findViewById(R.id.license_image);

        nameInput = findViewById(R.id.driver_name);
        emailInput = findViewById(R.id.driver_email);
        phoneInput = findViewById(R.id.driver_phone);

        Button chooseProfileImageButton = findViewById(R.id.choose_profile_image_button);
        Button chooseLicenseImageButton = findViewById(R.id.choose_license_image_button);
        Button registerDriverButton = findViewById(R.id.register_driver_button);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        chooseProfileImageButton.setOnClickListener(v -> showImagePickerOptions(REQUEST_IMAGE_PICK_PROFILE, REQUEST_IMAGE_CAMERA_PROFILE));
        chooseLicenseImageButton.setOnClickListener(v -> showImagePickerOptions(REQUEST_IMAGE_PICK_LICENSE, REQUEST_IMAGE_CAMERA_LICENSE));

        // Initialize Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Users").child("Customer").child(String.valueOf(mAuth.getCurrentUser().getUid()));

        // Fetch data from Firebase Realtime Database
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("ERR", reference.toString());
                // Get the value from the dataSnapshot
                String fetchedName = dataSnapshot.child("name").getValue(String.class);
                String fetchedEmail = dataSnapshot.child("email").getValue(String.class);
                String fetchedPhoneNumber = dataSnapshot.child("phoneNumber").getValue(String.class);

                name = fetchedName;
                email = fetchedEmail;
                phoneNumber = fetchedPhoneNumber;
                // Set the values in the edit text views
                nameInput.setText(fetchedName);
                emailInput.setText(fetchedEmail);
                phoneInput.setText(fetchedPhoneNumber);
                // Code to handle the rest of the registration process goes here
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("Firebase", "Failed to read value", error.toException());
            }
        });


        registerDriverButton.setOnClickListener(v -> {

            if (!name.isEmpty() && !email.isEmpty() && profileImageUri != null && licenseImageUri != null) {
                registerDriver(name, email, phoneNumber);
            } else {
                // Show error message
                Toast.makeText(DriverRegisterActivity.this, "Please complete all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    private void showImagePickerOptions(int requestGalleryCode, int requestCameraCode) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(DriverRegisterActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(DriverRegisterActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DriverRegisterActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera(requestCameraCode);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                if (ContextCompat.checkSelfPermission(DriverRegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(DriverRegisterActivity.this,
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
                if (requestCode == REQUEST_IMAGE_CAMERA_PROFILE) {
                    profileImageUri = imageUri;
                } else if (requestCode == REQUEST_IMAGE_CAMERA_LICENSE) {
                    licenseImageUri = imageUri;
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK_PROFILE || requestCode == REQUEST_IMAGE_PICK_LICENSE) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (requestCode == REQUEST_IMAGE_PICK_PROFILE) {
                        profileImageUri = selectedImageUri;
                        profileImageView.setImageURI(profileImageUri);
                    } else if (requestCode == REQUEST_IMAGE_PICK_LICENSE) {
                        licenseImageUri = selectedImageUri;
                        licenseImageView.setImageURI(licenseImageUri);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA_PROFILE) {
                profileImageView.setImageURI(profileImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAMERA_LICENSE) {
                licenseImageView.setImageURI(licenseImageUri);
            }
        }
    }

    private void registerDriver(String name, String email, String phoneNumber) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Driver driver = new Driver(name, email, phoneNumber, "", "", false, 0);

            // Upload profile image to Firebase Storage
            StorageReference profileImageRef = mStorage.child("drivers/" + uid + "/profile.jpg");
            profileImageRef.putFile(profileImageUri)
                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        driver.setProfilePicture(uri.toString());

                        // Upload license image to Firebase Storage
                        StorageReference licenseImageRef = mStorage.child("drivers/" + uid + "/license.jpg");
                        licenseImageRef.putFile(licenseImageUri)
                                .addOnSuccessListener(taskSnapshot1 -> licenseImageRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                    driver.setDriverLicensePicture(uri1.toString());

                                    // Save driver to Firebase Database
                                    mDatabase.child("Users").child("Drivers").child(uid).setValue(driver)
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    // Registration successful
                                                    AndroidUtil.showToast(DriverRegisterActivity.this, "Driver registers successfully");
                                                    finish();
                                                } else {
                                                    // Handle error
                                                }
                                            });
                                }))
                                .addOnFailureListener(e -> {
                                    // Handle error
                                });
                    }))
                    .addOnFailureListener(e -> {
                        // Handle error
                    });
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

//    private void uploadImages(String uid, String name, String email, String password) {
//        if (profileImageUri != null) {
//            StorageReference profileImageRef = mStorage.child("images/" + uid + "/profile.jpg");
//            profileImageRef.putFile(profileImageUri)
//                    .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
//                            .addOnSuccessListener(uri -> {
//                                String profileImageUrl = uri.toString();
//                                if (licenseImageUri != null) {
//                                    StorageReference licenseImageRef = mStorage.child("images/" + uid + "/license.jpg");
//                                    licenseImageRef.putFile(licenseImageUri)
//                                            .addOnSuccessListener(licenseTaskSnapshot -> licenseImageRef.getDownloadUrl()
//                                                    .addOnSuccessListener(licenseUri -> {
//                                                        String licenseImageUrl = licenseUri.toString();
//                                                        saveDriverToDatabase(uid, name, email, password, profileImageUrl, licenseImageUrl);
//                                                    }));
//                                }
//                            }));
//        }
//    }
//
//    private void saveDriverToDatabase(String uid, String name, String email, String password, String profileImageUrl, String licenseImageUrl) {
//        Driver driver = new Driver(name, email, password, profileImageUrl, licenseImageUrl, false, 0);
//        mDatabase.child("Users").child("Driver").child(uid).setValue(driver)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Registration success, finish activity
//                        finish();
//                    } else {
//                        // Handle failure
//                    }
//                });
//    }
}