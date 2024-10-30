package com.example.rideshare.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.adapter.RideAdapter;
import com.example.rideshare.entity.Ride;
import com.example.rideshare.utils.Haversine;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RideAdapter rideAdapter;
    private List<Ride> rideList;
    private TextView emptyTextView;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        initializeLoadingDialog();
        showLoadingDialog();
        emptyTextView = findViewById(R.id.emptyTextView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        rideList = new ArrayList<>();
        rideAdapter = new RideAdapter(this, rideList);
        recyclerView.setAdapter(rideAdapter);

        // Set item click listener
        rideAdapter.setOnItemClickListener(ride -> {
            Intent intent = new Intent(RideActivity.this, RideDetailActivity.class);
            intent.putExtra("seatsRequest", getIntent().getStringExtra("personCount"));
            intent.putExtra("ride", ride);
            startActivity(intent);
        });

        // Retrieve search data
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");
        String dateTime = getIntent().getStringExtra("dateTime");
        String person = getIntent().getStringExtra("personCount");

        double originLat = getIntent().getDoubleExtra("originLat", 0.0);
        double originLng = getIntent().getDoubleExtra("originLng", 0.0);
        double destinationLat = getIntent().getDoubleExtra("destinationLat", 0.0);
        double destinationLng = getIntent().getDoubleExtra("destinationLng", 0.0);

        // Fetch data from database
        fetchRides(originLat, originLng, destinationLat, destinationLng, origin, destination, dateTime, person);
    }

    private void fetchRides(double userOriginLat, double userOriginLng, double userDestinationLat, double userDestinationLng, String origin, String destination, String dateTime, String person) {
        // Use Firebase or other data source to get the list of rides
        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference().child("Rides");
        ridesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rideList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);

                    // Get the origin and destination lat/lng from the ride object
                    double rideOriginLat = ride.getOriginLatitude();
                    double rideOriginLng = ride.getOriginLongitude();
                    double rideDestinationLat = ride.getDestinationLatitude();
                    double rideDestinationLng = ride.getDestinationLongitude();

//                    // Extract date part from both dateTime and ride.getDate()
//                    String rideDate = extractDate(ride.getDate());
//                    String searchDate = extractDate(dateTime);
//                    if (ride.getOrigin().equals(origin) &&
//                        ride.getDestination().equals(destination) &&
//                        rideDate.equals(searchDate) &&
//                        ride.getSeatsAvailable() >= Integer.parseInt(person)) {
//
//                        String key = snapshot.getKey();
//                        ride.setRideId(key);
//                        rideList.add(ride);
//                    }
                    // Calculate distances
                    double originDistance = Haversine.calculateDistance(userOriginLat, userOriginLng, rideOriginLat, rideOriginLng);
                    double destinationDistance = Haversine.calculateDistance(userDestinationLat, userDestinationLng, rideDestinationLat, rideDestinationLng);

                    // Define a threshold for "close enough" (e.g., 5 km)
                    double threshold = 5.0; // in kilometers

                    String rideDate = extractDate(ride.getDate());
                    String searchDate = extractDate(dateTime);

                    if (originDistance <= threshold &&
                            destinationDistance <= threshold &&
                            rideDate.equals(searchDate) &&
                            ride.getSeatsAvailable() >= Integer.parseInt(person)) {

                        if (ride.getStatus().equalsIgnoreCase("pending")) {
                            ride.setRideId(snapshot.getKey());
                            rideList.add(ride);
                        }

                    }
                }
                rideAdapter.notifyDataSetChanged();
                updateUI();
                dismissLoadingDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });

    }
    private void updateUI() {
        if (rideList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    private void initializeLoadingDialog() {
        if (RideActivity.this != null) {
            loadingDialog = new Dialog(RideActivity.this);
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

    private String extractDate(String dateTime) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = dateTimeFormat.parse(dateTime);
            return date != null ? dateFormat.format(date) : "";
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
}