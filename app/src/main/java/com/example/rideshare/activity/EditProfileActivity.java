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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
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

public class EditProfileActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private EditText usernameEditText, emailEditText, mobileEditText;
    private ImageView profileImageView, updateProfileImage;
    private Button updateButton;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private StorageReference storageReference;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        profileImageView = findViewById(R.id.image_profile);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email_address);
        mobileEditText = findViewById(R.id.mobile_phone);
        updateButton = findViewById(R.id.publish_ride_button);
        updateProfileImage = findViewById(R.id.update_profile_image);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Customer").child(userId);
        }

        // Fetch user data from Firebase
        fetchUserData();

        // Set onClickListener to open image picker
        updateProfileImage.setOnClickListener(v -> showImagePickerOptions());

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
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);

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
                            Glide.with(EditProfileActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.ic_profile) // Placeholder image
                                    .circleCrop() // This will make the image circular
                                    .into(profileImageView);
                        } else {
                            // Set default image if no profile image exists
                            profileImageView.setImageResource(R.drawable.ic_profile);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                imageUri = data.getData();
                profileImageView.setImageURI(imageUri);
            } else if (requestCode == REQUEST_CAMERA) {
                profileImageView.setImageURI(imageUri);
            }
        }

    }

    private void updateUserData() {
        dialog = new Dialog(EditProfileActivity.this);
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

            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        } else {
            dialog.dismiss();
            Toast.makeText(EditProfileActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        String fileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference fileReference = storageReference.child(fileName);

        fileReference.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                    userDatabaseReference.child("profileImageUrl").setValue(uri1.toString());

                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                }))
                .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void showImagePickerOptions() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera();
                }
            } else if (options[item].equals("Choose from Gallery")) {
                if (ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    openImagePicker();
                }
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void openImagePicker() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CAMERA);
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = AndroidUtil.createImageFile(this);
            } catch (IOException ex) {
                Log.e("EditProfileActivity", "Error occurred while creating the File", ex);
            }
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.rideshare.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Storage permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}