package com.example.rideshare.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.adapter.VehicleAdapter;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Vehicle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DriverDetailActivity extends AppCompatActivity {

    private ImageView driverProfileImage, driverDetailLicenseImage;
    private TextView driverName, driverEmail, driverPhoneNumber, driverDetailJSON;
    private Button buttonActivate, buttonDeactivate;
    private DatabaseReference driverRef;

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail);

        driverProfileImage = findViewById(R.id.driverDetailProfileImage);
        driverDetailLicenseImage = findViewById(R.id.driverDetailLicenseImage);
        driverName = findViewById(R.id.driverDetailName);
        driverEmail = findViewById(R.id.driverDetailEmail);
        driverPhoneNumber = findViewById(R.id.driverDetailPhoneNumber);
//        driverDetailJSON = findViewById(R.id.driverDetailJSON);
        buttonActivate = findViewById(R.id.buttonActivate);
        buttonDeactivate = findViewById(R.id.buttonDeactivate);

        String driverId = getIntent().getStringExtra("driverId");
        driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);

        recyclerView = findViewById(R.id.recycler_view2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        vehicleList = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(vehicleList, driverId);
        recyclerView.setAdapter(vehicleAdapter);

        loadVehicles();

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Driver driver = dataSnapshot.getValue(Driver.class);
                if (driver != null) {
                    driverName.setText(driver.getName());
                    driverEmail.setText(driver.getEmail());
                    driverPhoneNumber.setText(driver.getPhoneNumber());
//                    driverDetailJSON.setText(dataSnapshot.getValue().toString());
                    Glide.with(DriverDetailActivity.this).load(driver.getProfilePicture()).into(driverProfileImage);
                    Glide.with(DriverDetailActivity.this).load(driver.getDriverLicensePicture()).into(driverDetailLicenseImage);

                    buttonActivate.setOnClickListener(v -> setDriverVerifiedStatus(true));
                    buttonDeactivate.setOnClickListener(v -> setDriverVerifiedStatus(false));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void setDriverVerifiedStatus(boolean isActive) {
        driverRef.child("verified").setValue(isActive);
    }

    private void loadVehicles() {
        driverRef.child("Vehicles").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vehicleList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Vehicle vehicle = snapshot.getValue(Vehicle.class);
                    vehicle.setVehicleId(snapshot.getKey());
                    if (vehicle.getStatus() == null) {
                        vehicleList.add(vehicle);
                    }

                }
                vehicleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}