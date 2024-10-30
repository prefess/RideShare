package com.example.rideshare.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.rideshare.adapter.DriverAdapter;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDrivers;
    private DriverAdapter driverAdapter;
    private DatabaseReference driversRef;
    private List<Driver> driverList;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initializeLoadingDialog();
        showLoadingDialog();
        recyclerViewDrivers = findViewById(R.id.recyclerViewDrivers);
        recyclerViewDrivers.setLayoutManager(new LinearLayoutManager(this));

        driverList = new ArrayList<>();
        driverAdapter = new DriverAdapter(this, driverList);
        driversRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");

        driverAdapter.setOnItemClickListener(driver -> {
            Intent intent = new Intent(AdminActivity.this, DriverDetailActivity.class);
            intent.putExtra("driverId", driver.getDriverId());
            startActivity(intent);
        });


        driversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear();
                for (DataSnapshot driverSnap : snapshot.getChildren()) {
                    Driver driver = driverSnap.getValue(Driver.class);
                    driver.setDriverId(driverSnap.getKey());
                    driverList.add(driver);
                }

                driverAdapter.notifyDataSetChanged();
                dismissLoadingDialog();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        recyclerViewDrivers.setAdapter(driverAdapter);
    }

    private void initializeLoadingDialog() {
        if (AdminActivity.this != null) {
            loadingDialog = new Dialog(AdminActivity.this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.loading_dialog);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            loadingDialog.setCancelable(false); // Prevents the dialog from being dismissed by the user
        }
    }

    private void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}