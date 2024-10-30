package com.example.rideshare.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Booking;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Ride;
import com.example.rideshare.entity.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class RideDetailActivity extends AppCompatActivity {

    private ImageView driverImageView;
    private TextView driverNameTextView, driverRatingTextView, ridePriceTextView, mapTextView;
    private TextView startPointTextView, endPointTextView, startTimeTextView, returnTimeTextView, carModelTextView;
    private TextView rideWithTextView, seatBooked;
    private String driverId;
    private Dialog dialog;
    private List<Driver> driverList;
    private List<Vehicle> vehicleList;

    private List<Ride.Stop> stops;
    private Button requestRideButton;
    private int seatsAvailable;
    private String person;
    private Ride ride;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        dialog = new Dialog(RideDetailActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        // Initialize views
        driverImageView = findViewById(R.id.driver_image);
        driverNameTextView = findViewById(R.id.driver_name);
        driverRatingTextView = findViewById(R.id.driver_rating);
        ridePriceTextView = findViewById(R.id.ride_price);
        startPointTextView = findViewById(R.id.start_point);
        endPointTextView = findViewById(R.id.end_point);
        startTimeTextView = findViewById(R.id.start_time);
        returnTimeTextView = findViewById(R.id.return_time);
        rideWithTextView = findViewById(R.id.ride_with);
        seatBooked = findViewById(R.id.seat_booked);
        carModelTextView = findViewById(R.id.model);
        mapTextView = findViewById(R.id.map_view);
        driverList = new ArrayList<>();
        vehicleList = new ArrayList<>();
        stops = new ArrayList<>();
        person = getIntent().getStringExtra("seatsRequest");

        // Get the passed Ride object
        ride = (Ride) getIntent().getSerializableExtra("ride");
        driverId = ride.getDriverId();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                driverList.clear();
                Driver driver = snapshot.getValue(Driver.class);
                driverList.add(driver);
                // Populate the data
                for (Driver driverLoop : driverList) {
                    float rating = (float)Math.round(driverLoop.getTotalRatingScore()*10)/10;
                    driverRatingTextView.setText(rating + " ⭐");
                    driverNameTextView.setText(driverLoop.getName());
                    Glide.with(RideDetailActivity.this).load(driverLoop.getProfilePicture()).into(driverImageView);

                }
                ridePriceTextView.setText(ride.getPrice() + " VND");
                startPointTextView.setText(ride.getOrigin());
                endPointTextView.setText(ride.getDestination());
                startTimeTextView.setText(ride.getDate()); // Update with appropriate format
//        returnTimeTextView.setText(ride.getReturnTime()); // Update with appropriate format
                rideWithTextView.setText(ride.getSeatsAvailable() + " seats available");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference stopRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("stops");
        stopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stops.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    stops.add(new Ride.Stop("", latitude, longitude));
                }
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stopsString = convertStopsToString(stops);
                Intent intent = new Intent(RideDetailActivity.this, RouteMapActivity.class);
                intent.putExtra("origin", ride.getOriginLatitude() + "," + ride.getOriginLongitude());
                intent.putExtra("destination", ride.getDestinationLatitude() + "," + ride.getDestinationLongitude());
                intent.putExtra("stops", stopsString); // Pass as JSON Array or another suitable format
                startActivity(intent);
            }
        });

        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum = 0;
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (!booking.getStatus().equals("canceled")) {
                        int seat = bookingSnapshot.child("bookedSeats").getValue(Integer.class);
                        sum += seat;
                    }
                }
                String textSeat = sum + " seat(s) booked";
                seatBooked.setText(textSeat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("Vehicles").child(ride.getVehicleId());
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vehicleList.clear();
                Vehicle vehicle = snapshot.getValue(Vehicle.class);
                vehicleList.add(vehicle);
                for (Vehicle vehicle1: vehicleList) {
                    String modelText = vehicle1.getVehicleType() + " | " + vehicle1.getVehicleColour();
                    carModelTextView.setText(modelText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestRideButton = findViewById(R.id.request_ride_button);
        requestRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRide();
            }
        });
    }

    private void requestRide() {
        String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if the current user is the driver
        if (customerId.equals(driverId)) {
            // Show a message and prevent the driver from booking their own ride
            Toast.makeText(RideDetailActivity.this, "You cannot book a ride as a driver for yourself.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the customer already has a pending or accepted request
        DatabaseReference userBookingsRef = FirebaseDatabase.getInstance().getReference().child("Bookings");
        userBookingsRef.orderByChild("customerId").equalTo(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasPendingOrAccepted = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && (booking.getStatus().equals("pending"))) {
                        hasPendingOrAccepted = true;
                        break;
                    }
                }

                if (hasPendingOrAccepted) {
                    Toast.makeText(RideDetailActivity.this, "You already have a pending booking.", Toast.LENGTH_SHORT).show();
                } else {
                    proceedWithBooking();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RideDetailActivity", "Failed to check existing bookings", databaseError.toException());
            }
        });
    }

    private void proceedWithBooking() {
        if (ride.getSeatsAvailable() > 0) {
            // Update seats available
            seatsAvailable = ride.getSeatsAvailable() - Integer.parseInt(person);
            DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());
            rideRef.child("seatsAvailable").setValue(seatsAvailable);

            // Create a new booking under the Rides node
            DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
            String bookingId = bookingsRef.push().getKey();
            String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//            String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();


            Booking newBooking = new Booking(bookingId, ride.getRideId(), customerId, "pending", Integer.parseInt(person));
            bookingsRef.child(bookingId).setValue(newBooking);

            // ALSO create a new booking under the Bookings node
            DatabaseReference bookingsClassRef = FirebaseDatabase.getInstance().getReference().child("Bookings").child(bookingId);
            bookingsClassRef.setValue(newBooking)
                    .addOnSuccessListener(aVoid -> {
                        // Log to confirm data has been written to Bookings
                        Log.d("RideDetailActivity", "Booking saved successfully to both Rides and Bookings nodes");

                        // Navigate to confirmation screen
                        FirebaseMessaging.getInstance().subscribeToTopic("customer_" + customerId)
                                .addOnCompleteListener(task -> {
                                    String msg = "Subscription to customer_" + customerId + " successful";
                                    if (!task.isSuccessful()) {
                                        msg = "Subscription failed";
                                    }
                                    Log.d("FCM", msg);
//                                    Toast.makeText(RideDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                                });
                        Intent intent = new Intent(RideDetailActivity.this, ConfirmRequestActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Log error if data writing fails
                        Log.e("RideDetailActivity", "Failed to save booking to Bookings node in Firebase", e);
                    });

//            // Navigate to confirmation screen
//            Intent intent = new Intent(RideDetailActivity.this, ConfirmationActivity.class);
//            startActivity(intent);
        } else {
            // Show message: No seats available
            Toast.makeText(RideDetailActivity.this, "No seats available", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertStopsToString(List<Ride.Stop> stops) {
        StringBuilder stopsStringBuilder = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            Ride.Stop stop = stops.get(i);
            stopsStringBuilder.append(stop.getLatitude()).append(",").append(stop.getLongitude());
            if (i < stops.size() - 1) {
                stopsStringBuilder.append("|"); // Delimiter between stops
            }
        }
        return stopsStringBuilder.toString();
    }
}