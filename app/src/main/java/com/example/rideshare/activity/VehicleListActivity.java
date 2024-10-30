package com.example.rideshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.adapter.VehicleAdapter;
import com.example.rideshare.entity.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VehicleListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> vehicleList;
    private DatabaseReference mDatabase;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_list);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        mAuth = FirebaseAuth.getInstance();
        String driverId = mAuth.getCurrentUser().getUid();
        vehicleList = new ArrayList<>();
        vehicleAdapter = new VehicleAdapter(vehicleList, driverId);
        recyclerView.setAdapter(vehicleAdapter);
        mDatabase = FirebaseDatabase.getInstance().getReference("Users").child("Drivers").child(mAuth.getCurrentUser().getUid()).child("Vehicles");

        loadVehicles();

        Button addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(VehicleListActivity.this, AddVehicleActivity.class);
            startActivity(intent);
        });
    }

    private void loadVehicles() {
        mDatabase.addValueEventListener(new ValueEventListener() {
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