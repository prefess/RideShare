package com.example.rideshare.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.adapter.BookingRequestAdapter;
import com.example.rideshare.entity.Booking;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Ride;
import com.example.rideshare.entity.Vehicle;
import com.example.rideshare.fragment.PublishFragment;
import com.example.rideshare.fragment.PublishedRideFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

public class DriverRideDetailActivity extends AppCompatActivity {

    private ImageView driverImageView;
    private TextView driverNameTextView, driverRatingTextView, ridePriceTextView, mapTextView;
    private TextView startPointTextView, endPointTextView, startTimeTextView, returnTimeTextView, carModelTextView;
    private TextView rideWithTextView, seatBooked;
    private String driverId;
    private List<Driver> driverList;
    private List<Vehicle> vehicleList;

    private List<Ride.Stop> stops;
    private Button cancelRideButton, requestListButton;

    private Button startRideButton, endRideButton;
    private int seatsAvailable;
    private String person;
    private Ride ride;
    private Dialog dialog;
    private List<Booking> bookingList, bookingListAccepted;

    private BottomSheetDialog bottomSheetDialog;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_ride_detail);

        dialog = new Dialog(DriverRideDetailActivity.this);
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
        requestListButton = findViewById(R.id.request_list_button);

        startRideButton = findViewById(R.id.start_ride_button);
        endRideButton = findViewById(R.id.end_ride_button);

        carModelTextView = findViewById(R.id.model);
        mapTextView = findViewById(R.id.map_view);
        driverList = new ArrayList<>();
        vehicleList = new ArrayList<>();
        stops = new ArrayList<>();
        bookingList = new ArrayList<>();
        bookingListAccepted = new ArrayList<>();
        person = getIntent().getStringExtra("seatsRequest");


        // Initialize FusedLocationProviderClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    Location location = locationResult.getLastLocation();
                    // Get the latitude and longitude from the location
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    // Use the latitude and longitude as needed
                    updateDriverLocation(location); // Share the location with customers
                }
            }
        };
        // Initialize location request
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        startRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRide();
            }
        });

        endRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endRide();
            }
        });

        // Get the passed Ride object
        ride = (Ride) getIntent().getSerializableExtra("ride");
//        if (ride.getStatus().equalsIgnoreCase("processing")) {
//            startRide();
//        }

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
                    Glide.with(DriverRideDetailActivity.this).load(driverLoop.getProfilePicture()).into(driverImageView);

                }
                ridePriceTextView.setText(ride.getPrice() + " VND");
                startPointTextView.setText(ride.getOrigin());
                endPointTextView.setText(ride.getDestination());
                startTimeTextView.setText(ride.getDate()); // Update with appropriate format
//        returnTimeTextView.setText(ride.getReturnTime()); // Update with appropriate format
                rideWithTextView.setText(ride.getSeatsAvailable() + " people");

                if (ride!=null && ride.getStatus().equalsIgnoreCase("completed")) {
                    cancelRideButton.setVisibility(View.GONE);
                    requestListButton.setVisibility(View.GONE);
                    startRideButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference bookingListRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
        bookingListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (booking.getStatus().equals("accepted")) {
                            bookingListAccepted.add(booking);
                    }
                }
                seatBooked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ride != null && ride.getSeatsAvailable() > 0) {
                            Intent intent = new Intent(DriverRideDetailActivity.this, GuestListActivity.class);
                            intent.putExtra("guest_list", new ArrayList<>(bookingListAccepted));
                            startActivity(intent);
                        } else {
                            Toast.makeText(DriverRideDetailActivity.this, "No guests available", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                int sum = 0;
                int countBooked = 0;
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (!booking.getStatus().equals("canceled")) {
                        if (booking.getStatus().equals("pending")) {
                            bookingList.add(booking);
                            countBooked++;
                        }
                        int seat = bookingSnapshot.child("bookedSeats").getValue(Integer.class);
                        sum += seat;
                    }
                }
                String textSeat = sum + " seat(s) booked";
                seatBooked.setText(textSeat);
                String requestCount = "Request (" + countBooked + ")";
                requestListButton.setText(requestCount);
                long currentTime = System.currentTimeMillis();
                String stringDate = ride.getDate();
                long rideTime = convertDateToTimestamp(stringDate) + 43200000; // Convert ride.getDate() to a timestamp

                if(rideTime <= currentTime) {
                    cancelRideButton.setVisibility(View.GONE);
                    requestListButton.setVisibility(View.GONE);
                    startRideButton.setVisibility(View.GONE);
                    endRideButton.setVisibility(View.GONE);
                }
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
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
                Intent intent = new Intent(DriverRideDetailActivity.this, RouteMapActivity.class);
                intent.putExtra("origin", ride.getOriginLatitude() + "," + ride.getOriginLongitude());
                intent.putExtra("destination", ride.getDestinationLatitude() + "," + ride.getDestinationLongitude());
                intent.putExtra("stops", stopsString); // Pass as JSON Array or another suitable format
                startActivity(intent);
            }
        });

        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("Vehicles").child(ride.getVehicleId());
        vehicleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vehicleList.clear();
                Vehicle vehicle = snapshot.getValue(Vehicle.class);
                vehicleList.add(vehicle);
                for (Vehicle vehicle1 : vehicleList) {
                    String modelText = vehicle1.getVehicleType() + " | " + vehicle1.getVehicleColour();
                    carModelTextView.setText(modelText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        cancelRideButton = findViewById(R.id.cancel_ride_button);
        cancelRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DriverRideDetailActivity.this)
                        .setTitle("Cancel Ride")
                        .setMessage("Are you sure you want to cancel your ride?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                cancelRide();
                            }
                        })
                        .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
            }
        });

        requestListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBookingRequests();
            }
        });

    }

    private void cancelRide() {
        // Update ride status to cancel
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());
        rideRef.child("status").setValue("canceled");
        DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference().child("Bookings");
        bookRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bookSnap : snapshot.getChildren()) {
                    String rideId2 = bookSnap.child("rideId").getValue(String.class);
                    if (rideId2.equalsIgnoreCase(ride.getRideId())) {
                        String bookingId = bookSnap.child("bookingId").getValue(String.class);
                        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference()
                                .child("Bookings")
                                .child(bookingId);  // Assuming booking has a bookingId field
                        bookingRef.child("status").setValue("rideCanceled");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        finish();
//            // Navigate to confirmation screen
//            Intent intent = new Intent(RideDetailActivity.this, ConfirmationActivity.class);
//            startActivity(intent);
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

    private void showBookingRequests() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_booking_requests, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        RecyclerView recyclerView = bottomSheetView.findViewById(R.id.booking_requests_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        BookingRequestAdapter adapter = new BookingRequestAdapter(this, bookingList, new BookingRequestAdapter.OnBookingActionListener() {
            @Override
            public void onAccept(Booking booking) {
                // Handle Accept
//                acceptBooking(booking);
                updateBookingStatus(booking, "accepted");
            }

            @Override
            public void onDecline(Booking booking) {
                // Handle Decline
//                declineBooking(booking);
                updateBookingStatus(booking, "canceled");
            }
        });

        recyclerView.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void updateBookingStatus(Booking booking, String status) {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference()
                .child("Bookings")
                .child(booking.getBookingId());  // Assuming booking has a bookingId field

        DatabaseReference rideBookingRef = FirebaseDatabase.getInstance().getReference()
                .child("Rides")
                .child(booking.getRideId())
                .child("bookings")
                .child(booking.getBookingId());  // Assuming bookings are stored under the Ride's bookings node

        bookingRef.child("status").setValue(status)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update status in the Rides node as well
                        rideBookingRef.child("status").setValue(status)
                                .addOnCompleteListener(rideTask -> {
                                    if (rideTask.isSuccessful()) {
                                        if (status.equals("accepted")) {
                                            updateSeatAvailability(booking, true);
                                        } else {
                                            updateSeatAvailability(booking, false);
                                        }
                                        // Refresh the UI
                                        refreshBookingList();
                                    } else {
                                        // Handle failure to update the Rides node
                                    }
                                });
                    } else {
                        // Handle failure
                    }
                });
    }

    private void updateSeatAvailability(Booking booking, boolean isAccepted) {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference()
                .child("Rides")
                .child(booking.getRideId());  // Assuming booking has a rideId field

        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int currentAvailableSeats = snapshot.child("seatsAvailable").getValue(Integer.class);
                    int updatedSeats = currentAvailableSeats;

                    if (isAccepted) {
//                        updatedSeats -= booking.getBookedSeats();  // Deduct booked seats from available seats
                    } else {
                        updatedSeats += booking.getBookedSeats();  // Add seats back to available seats
                    }

                    rideRef.child("seatsAvailable").setValue(updatedSeats);

                    // Update the UI to reflect the new seat availability
                    rideWithTextView.setText(updatedSeats + " seats available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void refreshBookingList() {
        DatabaseReference bookingRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
        bookingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bookingList.clear();
                int sum = 0;
                int countBooked = 0;
                for (DataSnapshot bookingSnapshot : snapshot.getChildren()) {
                    Booking booking = bookingSnapshot.getValue(Booking.class);
                    if (!booking.getStatus().equals("canceled")) {
                        if (booking.getStatus().equals("pending")) {
                            bookingList.add(booking);
                            countBooked++;
                        }
                        int seat = bookingSnapshot.child("bookedSeats").getValue(Integer.class);
                        sum += seat;
                    }
                }
                String textSeat = sum + " seat(s) booked";
                seatBooked.setText(textSeat);
                String requestCount = "Request (" + countBooked + ")";
                requestListButton.setText(requestCount);

                // Notify the adapter if the BottomSheet is still open
                RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.booking_requests_recycler_view);
                if (recyclerView != null && recyclerView.getAdapter() != null) {
                    recyclerView.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startRide() {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());
        rideRef.child("bookings").addListenerForSingleValueEvent(new ValueEventListener() {
            int pending = 0;
            int accepted = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot rideSnap : snapshot.getChildren()) {
                    String status = rideSnap.child("status").getValue(String.class);
                    if (status.equalsIgnoreCase("pending")) {
                        pending++;
                    }
                    if (status.equalsIgnoreCase("accepted")) {
                        accepted++;
                    }
                }
                if (pending == 0 && accepted >= 1) {
                    // Update ride status to "processing"
                    rideRef.child("status").setValue("processing").addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(DriverRideDetailActivity.this, "Ride started!", Toast.LENGTH_SHORT).show();
                            startRideButton.setVisibility(View.GONE);
                            endRideButton.setVisibility(View.VISIBLE);
                            cancelRideButton.setEnabled(false);
                            cancelRideButton.setBackgroundColor(getResources().getColor(com.google.android.gms.base.R.color.common_google_signin_btn_text_dark_disabled));
                            requestListButton.setEnabled(false);
                            requestListButton.setBackgroundColor(getResources().getColor(com.google.android.gms.base.R.color.common_google_signin_btn_text_dark_disabled));

                            // Share driver's location with customers (you need to implement this method)
                            startLocationUpdates();
                        } else {
                            Toast.makeText(DriverRideDetailActivity.this, "Failed to start ride", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void endRide() {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId());

        // Update ride status to "completed"
        rideRef.child("status").setValue("completed").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DriverRideDetailActivity.this, "Ride ended!", Toast.LENGTH_SHORT).show();
                endRideButton.setEnabled(false);
                endRideButton.setBackgroundColor(getResources().getColor(com.google.android.gms.base.R.color.common_google_signin_btn_text_dark_disabled));
                cancelRideButton.setEnabled(false);
                cancelRideButton.setBackgroundColor(getResources().getColor(com.google.android.gms.base.R.color.common_google_signin_btn_text_dark_disabled));
                requestListButton.setEnabled(false);
                requestListButton.setBackgroundColor(getResources().getColor(com.google.android.gms.base.R.color.common_google_signin_btn_text_dark_disabled));

                // Stop sharing driver's location (you need to implement this method)
                stopLocationUpdates();
                Intent intent = new Intent(DriverRideDetailActivity.this, EndedRideActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(DriverRideDetailActivity.this, "Failed to end ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void updateDriverLocation(Location location) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference()
                .child("Rides")
                .child(ride.getRideId())
                .child("driverLocation");

        locationRef.setValue(location);
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
}
