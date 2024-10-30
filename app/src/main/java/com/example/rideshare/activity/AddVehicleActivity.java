package com.example.rideshare.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.rideshare.R;
import com.example.rideshare.entity.Vehicle;
import com.example.rideshare.utils.AndroidUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddVehicleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SELECT_VEHICLE_IMAGE = 1;
    private static final int REQUEST_CODE_SELECT_INSURANCE_IMAGE = 2;

    private static final int REQUEST_IMAGE_CAMERA_VEHICLE = 3;
    private static final int REQUEST_IMAGE_CAMERA_INSURANCE = 4;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_STORAGE_PERMISSION = 200;

    private ImageView vehicleImageView;
    private ImageView insuranceImageView;
    private Uri vehicleImageUri;
    private Uri insuranceImageUri;

    private EditText vehicleNameInput;
    private EditText vehicleTypeInput;
    private EditText vehicleRegNumberInput;
    private EditText vehicleColourInput;
    private EditText seatsOfferingInput;
    private Button addVehicleButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        vehicleImageView = findViewById(R.id.vehicle_image);
        insuranceImageView = findViewById(R.id.insurance_image);
        vehicleNameInput = findViewById(R.id.vehicle_name_input);
        vehicleTypeInput = findViewById(R.id.vehicle_type_input);
        vehicleRegNumberInput = findViewById(R.id.vehicle_reg_number_input);
        vehicleColourInput = findViewById(R.id.vehicle_colour_input);
        seatsOfferingInput = findViewById(R.id.seats_offering_input);
        addVehicleButton = findViewById(R.id.add_vehicle_button);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        vehicleImageView.setOnClickListener(v -> showImagePickerOptions(REQUEST_CODE_SELECT_VEHICLE_IMAGE, REQUEST_IMAGE_CAMERA_VEHICLE));
        insuranceImageView.setOnClickListener(v -> showImagePickerOptions(REQUEST_CODE_SELECT_INSURANCE_IMAGE, REQUEST_IMAGE_CAMERA_INSURANCE));
        addVehicleButton.setOnClickListener(v -> addVehicle());
    }

    private void openGallery(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, requestCode);
    }

    private void showImagePickerOptions(int requestGalleryCode, int requestCameraCode) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(AddVehicleActivity.this);
        builder.setTitle("Choose your profile picture");

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                if (ContextCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddVehicleActivity.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                } else {
                    openCamera(requestCameraCode);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                if (ContextCompat.checkSelfPermission(AddVehicleActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddVehicleActivity.this,
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
                if (requestCode == REQUEST_IMAGE_CAMERA_VEHICLE) {
                    vehicleImageUri = imageUri;
                } else if (requestCode == REQUEST_IMAGE_CAMERA_INSURANCE) {
                    insuranceImageUri = imageUri;
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, requestCode);
            }
        }
    }

    private void selectVehicleImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_VEHICLE_IMAGE);
    }

    private void selectInsuranceImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_INSURANCE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode ==REQUEST_CODE_SELECT_VEHICLE_IMAGE || requestCode == REQUEST_CODE_SELECT_INSURANCE_IMAGE) {
                if (data != null) {
                    Uri selectedImageUri = data.getData();
                    if (requestCode == REQUEST_CODE_SELECT_VEHICLE_IMAGE) {
                        vehicleImageUri = selectedImageUri;
                        vehicleImageView.setImageURI(vehicleImageUri);
                    } else if (requestCode == REQUEST_CODE_SELECT_INSURANCE_IMAGE) {
                        insuranceImageUri = selectedImageUri;
                        insuranceImageView.setImageURI(insuranceImageUri);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_CAMERA_VEHICLE) {
                vehicleImageView.setImageURI(vehicleImageUri);
            } else if (requestCode == REQUEST_IMAGE_CAMERA_INSURANCE) {
                insuranceImageView.setImageURI(insuranceImageUri);
            }
        }
    }

    private void addVehicle() {
        dialog = new Dialog(AddVehicleActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();


        String vehicleName = vehicleNameInput.getText().toString().trim();
        String vehicleType = vehicleTypeInput.getText().toString().trim();
        String vehicleRegNumber = vehicleRegNumberInput.getText().toString().trim();
        String vehicleColour = vehicleColourInput.getText().toString().trim();
        String seatsOffering = seatsOfferingInput.getText().toString().trim();

        if (vehicleName.isEmpty() || vehicleType.isEmpty() || vehicleRegNumber.isEmpty() ||
                vehicleColour.isEmpty() || seatsOffering.isEmpty() || vehicleImageUri == null || insuranceImageUri == null) {
            Toast.makeText(this, "Please fill all fields and select images", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String vehicleId = UUID.randomUUID().toString();

            StorageReference vehicleImageRef = mStorage.child("vehicle_images").child(vehicleId);
            StorageReference insuranceImageRef = mStorage.child("insurance_images").child(vehicleId);

            vehicleImageRef.putFile(vehicleImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return vehicleImageRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri vehicleImageUrl = task.getResult();
                            insuranceImageRef.putFile(insuranceImageUri)
                                    .continueWithTask(task2 -> {
                                        if (!task2.isSuccessful()) {
                                            throw task2.getException();
                                        }
                                        return insuranceImageRef.getDownloadUrl();
                                    })
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Uri insuranceImageUrl = task2.getResult();
                                            saveVehicleInfo(userId, vehicleId, vehicleName, vehicleType, vehicleRegNumber, vehicleColour, seatsOffering, vehicleImageUrl.toString(), insuranceImageUrl.toString());
                                        } else {
                                            Toast.makeText(AddVehicleActivity.this, "Failed to upload insurance image", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(AddVehicleActivity.this, "Failed to upload vehicle image", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
        }
    }

    private void saveVehicleInfo(String userId, String vehicleId, String vehicleName, String vehicleType, String vehicleRegNumber, String vehicleColour, String seatsOffering, String vehicleImageUrl, String insuranceImageUrl) {
        Map<String, Object> vehicleData = new HashMap<>();
        vehicleData.put("vehicleName", vehicleName);
        vehicleData.put("vehicleType", vehicleType);
        vehicleData.put("vehicleRegNumber", vehicleRegNumber);
        vehicleData.put("vehicleColour", vehicleColour);
        vehicleData.put("seatsOffering", seatsOffering);
        vehicleData.put("vehicleImageUrl", vehicleImageUrl);
        vehicleData.put("insuranceImageUrl", insuranceImageUrl);

        mDatabase.child("Users").child("Drivers").child(userId).child("Vehicles").child(vehicleId).setValue(vehicleData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddVehicleActivity.this, "Vehicle added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(AddVehicleActivity.this, "Failed to add vehicle", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
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