package com.example.rideshare.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Rating;
import com.example.rideshare.entity.Ride;
import com.example.rideshare.entity.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
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

public class BookedRideDetailActivity extends AppCompatActivity {

    private ImageView driverImageView;
    private TextView driverNameTextView, driverRatingTextView, ridePriceTextView, mapTextView;
    private TextView startPointTextView, endPointTextView, startTimeTextView, returnTimeTextView, carModelTextView;
    private TextView rideWithTextView;
    private String driverId;
    private List<Driver> driverList;
    private List<Vehicle> vehicleList;
    private List<Ride.Stop> stops;
    private Button rateYourRideButton, cancelRideButton, trackDriverButton;
    private int seatsAvailable;
    private String person;
    private Ride ride;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_ride_detail);

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
        carModelTextView = findViewById(R.id.model);

        mapTextView = findViewById(R.id.map_view);

        rateYourRideButton = findViewById(R.id.rate_your_ride_button);
        cancelRideButton = findViewById(R.id.cancel_ride_button);
        trackDriverButton = findViewById(R.id.track_driver_button);

        stops = new ArrayList<>();
        driverList = new ArrayList<>();
        vehicleList = new ArrayList<>();

        // Get ride details passed from BookedRideFragment
        ride = (Ride) getIntent().getSerializableExtra("ride");
        driverId = ride.getDriverId();

        // Load driver data and vehicle data
        loadDriverData();
        loadVehicleData();

        checkRideCompletion();

        checkRideStatus();

        rateYourRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRateDialog(ride);  // Show the rate dialog when button is clicked
            }
        });

        cancelRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(BookedRideDetailActivity.this)
                        .setTitle("Cancel Ride")
                        .setMessage("Are you sure you want to cancel your ride?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelRide(ride);
                            }
                        })
                        .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });

        trackDriverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logic to track the driver
                trackDriver();
            }
        });

        DatabaseReference stopRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("stops");
        stopRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                stops.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    stops.add(new Ride.Stop("", latitude, longitude));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stopsString = convertStopsToString(stops);
                Intent intent = new Intent(BookedRideDetailActivity.this, RouteMapActivity.class);
                intent.putExtra("origin", ride.getOriginLatitude() + "," + ride.getOriginLongitude());
                intent.putExtra("destination", ride.getDestinationLatitude() + "," + ride.getDestinationLongitude());
                intent.putExtra("stops", stopsString); // Pass as JSON Array or another suitable format
                startActivity(intent);
            }
        });
    }

    private void checkRideCompletion() {
        long currentTime = System.currentTimeMillis();
        long rideEndTime = convertDateToTimestamp(ride.getDate())+432000;

        if (currentTime > rideEndTime) {
            // Ride is complete, show "Rate Your Ride" button
            rateYourRideButton.setVisibility(View.VISIBLE);
            cancelRideButton.setVisibility(View.GONE);
        } else {
            // Ride is not complete, show "Cancel Ride" button
            cancelRideButton.setVisibility(View.VISIBLE);
            rateYourRideButton.setVisibility(View.GONE);
        }
    }

    private long convertDateToTimestamp(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        try {
            Date date = format.parse(dateString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void showRateDialog(Ride ride) {
        // Inflate the dialog view
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_rate_ride);

        // Find the RatingBar and EditText in the dialog
        RatingBar ratingBar = dialog.findViewById(R.id.ratingBar);
        EditText etComment = dialog.findViewById(R.id.etComment);
        Button submitButton = dialog.findViewById(R.id.btnSubmit);

        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();
            submitRating(ride, rating, comment);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitRating(Ride ride, float rating, String comment) {
        // Assuming the Ride object contains the driverId or similar identifier
        String driverId = ride.getDriverId();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's ID
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId);

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Driver driver = dataSnapshot.getValue(Driver.class);

                if (driver != null) {
                    if (driver.getTotalRatings() != 0) {
                        // Update the driver's rating (you may need to implement your own logic to calculate the average rating)
                        float newRating = calculateNewAverageRating(driver.getTotalRatingScore(), rating, driver.getTotalRatings());

                        // Update driver object with new rating and count
                        driver.setTotalRatingScore(newRating);
                        driver.setTotalRatings(driver.getTotalRatings() + 1);

                        // Save the updated driver object back to the database without losing other fields
                        driverRef.child("totalRatingScore").setValue(driver.getTotalRatingScore());
                        driverRef.child("totalRatings").setValue(driver.getTotalRatings());

                        // Save the rating and comment, including the user ID
                        String ratingId = driverRef.child("ratings").push().getKey();
                        Rating newRatingEntry = new Rating(currentUserId, rating, comment, ride.getRideId());
                        driverRef.child("ratings").child(ratingId).setValue(newRatingEntry);

                        Toast.makeText(BookedRideDetailActivity.this, "Rating submitted successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update the driver's rating (you may need to implement your own logic to calculate the average rating)
                        float newRating = rating;

                        // Update driver object with new rating and count
                        driver.setTotalRatingScore(newRating);
                        driver.setTotalRatings(driver.getTotalRatings() + 1);

                        // Save the updated driver object back to the database without losing other fields
                        driverRef.child("totalRatingScore").setValue(driver.getTotalRatingScore());
                        driverRef.child("totalRatings").setValue(driver.getTotalRatings());

                        // Save the rating and comment, including the user ID
                        String ratingId = driverRef.child("ratings").push().getKey();
                        Rating newRatingEntry = new Rating(currentUserId, rating, comment, ride.getRideId());
                        driverRef.child("ratings").child(ratingId).setValue(newRatingEntry);

                        Toast.makeText(BookedRideDetailActivity.this, "Rating submitted successfully.", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(BookedRideDetailActivity.this, "Failed to submit rating.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to calculate the new average rating
    private float calculateNewAverageRating(float currentRating, float newRating, int ratingCount) {
        return (currentRating * ratingCount + newRating) / (ratingCount + 1)*10/10;
    }

    private void cancelRide(Ride ride) {
        // Implement the logic to cancel the ride, such as updating Firebase Database
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());
        rideRef.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bookSnap : snapshot.getChildren()) {
                    if (bookSnap.child("customerId").getValue(String.class).equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        bookId = bookSnap.child("bookingId").getValue(String.class);
                    }
                }
                // Get references to the relevant parts of the database
                DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference().child("Bookings").child(bookId);
                DatabaseReference rideBookingRef = rideRef.child("bookings").child(bookId);

                // Step 1: Update the booking status to "canceled" in both places
                bookingRef.child("status").setValue("canceled");
                rideBookingRef.child("status").setValue("canceled");

                // Step 2: Increase the seatsAvailable by adding seatsBooked to it
                rideBookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int seatsBooked = snapshot.child("bookedSeats").getValue(Integer.class);

                            // Retrieve the current seatsAvailable and update it
                            rideRef.child("seatsAvailable").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        int currentSeatsAvailable = snapshot.getValue(Integer.class);
                                        int newSeatsAvailable = currentSeatsAvailable + seatsBooked;

                                        // Update the seatsAvailable in the database
                                        rideRef.child("seatsAvailable").setValue(newSeatsAvailable).addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(BookedRideDetailActivity.this, "Ride canceled successfully.", Toast.LENGTH_SHORT).show();
                                                finish(); // Close the activity or refresh the UI
                                            } else {
                                                Toast.makeText(BookedRideDetailActivity.this, "Failed to update seats availability.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(BookedRideDetailActivity.this, "Failed to retrieve current seats available.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookedRideDetailActivity.this, "Failed to retrieve booking details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        rideRef.child("status").setValue("canceled").addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                Toast.makeText(BookedRideDetailActivity.this, "Ride canceled successfully.", Toast.LENGTH_SHORT).show();
//                // Optionally, finish the activity or refresh the UI
//                finish();
//            } else {
//                Toast.makeText(BookedRideDetailActivity.this, "Failedto cancel the ride.", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void loadDriverData() {
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
                    Glide.with(BookedRideDetailActivity.this).load(driverLoop.getProfilePicture()).into(driverImageView);

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
    }

    private void loadVehicleData() {
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
    }

    private void checkRideStatus() {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rideStatus = snapshot.child("status").getValue(String.class);

                    if ("processing".equalsIgnoreCase(rideStatus)) {
                        // Ride is in progress
                        trackDriverButton.setVisibility(View.VISIBLE);
                        cancelRideButton.setVisibility(View.GONE);
                        rateYourRideButton.setVisibility(View.GONE);
                    } else if ("pending".equalsIgnoreCase(rideStatus)) {
                        // Ride is pending
                        cancelRideButton.setVisibility(View.VISIBLE);
                        trackDriverButton.setVisibility(View.GONE);
                        rateYourRideButton.setVisibility(View.GONE);
                    } else if ("completed".equalsIgnoreCase(rideStatus)) {
                        // Ride is complete
                        trackDriverButton.setVisibility(View.GONE);
                        cancelRideButton.setVisibility(View.GONE);
                        checkIfCustomerHasRatedRide();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(BookedRideDetailActivity.this, "Failed to retrieve ride status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfCustomerHasRatedRide() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverId).child("ratings");

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasRated = false;
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Rating rating = ratingSnapshot.getValue(Rating.class);
                    if (rating != null && rating.getUserId().equals(currentUserId) && rating.getRideId().equals(ride.getRideId())) {
                        hasRated = true;
                        break;
                    }
                }
                if (hasRated) {
                    rateYourRideButton.setVisibility(View.GONE);
                } else {
                    rateYourRideButton.setVisibility(View.VISIBLE);
                }
                cancelRideButton.setVisibility(View.GONE);
                trackDriverButton.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });
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

    private void trackDriver() {
        // Open the map activity with the driver's location
        Intent intent = new Intent(BookedRideDetailActivity.this, TrackDriverActivity.class);
        String stopsString = convertStopsToString(stops);
        intent.putExtra("origin", ride.getOriginLatitude() + "," + ride.getOriginLongitude());
        intent.putExtra("destination", ride.getDestinationLatitude() + "," + ride.getDestinationLongitude());
        intent.putExtra("stops", stopsString); // Pass as JSON Array or another suitable format
        intent.putExtra("rideId", ride.getRideId());
        startActivity(intent);
    }
}