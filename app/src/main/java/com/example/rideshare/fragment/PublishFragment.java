package com.example.rideshare.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.rideshare.R;
import com.example.rideshare.activity.ConfirmationActivity;
import com.example.rideshare.activity.LocationPickerActivity;
import com.example.rideshare.entity.Ride;
import com.example.rideshare.utils.AndroidUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class PublishFragment extends Fragment {

    private static final int START_POINT_REQUEST_CODE = 1;
    private static final int END_POINT_REQUEST_CODE = 2;
    private int stopRequestCodeBase = 100; // Base request code for stops
    private AutoCompleteTextView startPointAutoComplete, endPointAutoComplete;
    private EditText dateTimeEditText;
    private Spinner passengersSpinner;
    private EditText priceEditText;
    private Spinner vehicleSpinner;
    private Button publishRideButton;
    private LinearLayout stopsContainer;
    private DatabaseReference mDatabase;
    private Calendar calendar;
    private List<String> vehicleList;
    private List<String> vehicleIds;
    private List<Integer> stopRequestCodes = new ArrayList<>(); // Store stop request codes

    private String selectedVehicleId;
    private String driverId;
    private FragmentManager fragmentManager;

    private double startPointLat, startPointLng, endPointLat, endPointLng;
    private List<Double> stopLatitudes = new ArrayList<>();
    private List<Double> stopLongitudes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publish, container, false);

        startPointAutoComplete = view.findViewById(R.id.start_point_auto_complete);
        endPointAutoComplete = view.findViewById(R.id.end_point_auto_complete);
        dateTimeEditText = view.findViewById(R.id.date_time_edit_text);
        passengersSpinner = view.findViewById(R.id.passengers_spinner);
        priceEditText = view.findViewById(R.id.price_edit_text);
        vehicleSpinner = view.findViewById(R.id.vehicle_spinner);
        publishRideButton = view.findViewById(R.id.publish_ride_button);
        stopsContainer = view.findViewById(R.id.stops_container);

        calendar = Calendar.getInstance();
        vehicleList = new ArrayList<>();
        vehicleIds = new ArrayList<>();
        fragmentManager = getParentFragmentManager();
        driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchVehicles();

        dateTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker();
            }
        });

        startPointAutoComplete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
            startActivityForResult(intent, START_POINT_REQUEST_CODE);
        });

        endPointAutoComplete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
            startActivityForResult(intent, END_POINT_REQUEST_CODE);
        });

        Button addStopButton = view.findViewById(R.id.add_stop_button);
        addStopButton.setOnClickListener(v -> addStopField());

        publishRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checkDriverVerificationStatus();
                if(validateInput()) {
                    checkDriverStatus();
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();

//        setupAutoCompleteTextView(startPointAutoComplete);
//        setupAutoCompleteTextView(endPointAutoComplete);

        return view;
    }

    private void addStopField() {
        AutoCompleteTextView stopAutoComplete = new AutoCompleteTextView(getContext());
//        stopAutoComplete.setHint("Enter stop");
        Drawable myDrawable = getResources().getDrawable(R.drawable.ic_location_start);
        stopAutoComplete.setCompoundDrawablesWithIntrinsicBounds(myDrawable, null,null,null);
        stopAutoComplete.setSelected(true);
        stopAutoComplete.setBackgroundResource(android.R.color.transparent);
        stopAutoComplete.requestFocusFromTouch();
        stopAutoComplete.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        int currentStopRequestCode = stopRequestCodeBase++;
        stopRequestCodes.add(currentStopRequestCode);

        stopAutoComplete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), LocationPickerActivity.class);
            startActivityForResult(intent, currentStopRequestCode);
        });

        stopsContainer.addView(stopAutoComplete);
    }

    private void showDateTimePicker() {
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year1, month1, dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth);
                    showTimePicker();
                }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, minute1) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute1);

                    String dateTime = String.format("%02d-%02d-%04d %02d:%02d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH) + 1,
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE));

                    dateTimeEditText.setText(dateTime);
                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void fetchVehicles() {
        Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.primary_text_dark_nodisable);
        dialog.show();
//        showLoadingFragment();
        DatabaseReference vehicleRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverId).child("Vehicles");
        vehicleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                vehicleList.clear();
                vehicleIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String vehicleName = snapshot.child("vehicleType").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    if (status!= null && status.equalsIgnoreCase("disabled")) {

                    } else {
                        vehicleList.add(vehicleName);
                        vehicleIds.add(snapshot.getKey());
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, vehicleList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                vehicleSpinner.setAdapter(adapter);
//                hideLoadingFragment();
                dialog.dismiss();
                vehicleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedVehicleId = vehicleIds.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No action needed
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("fetchVehicles", "Error fetching vehicles: " + databaseError.getMessage());
                // Handle possible errors
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            // Get the latitude and longitude from the result Intent
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            String selectedAddress = data.getStringExtra("selectedAddress");

            if (requestCode == START_POINT_REQUEST_CODE) {
                startPointAutoComplete.setText(selectedAddress);
                startPointLat = latitude;
                startPointLng = longitude;
            } else if (requestCode == END_POINT_REQUEST_CODE) {
                endPointAutoComplete.setText(selectedAddress);
                endPointLat = latitude;
                endPointLng = longitude;
            } else if (stopRequestCodes.contains(requestCode)) {
                int stopIndex = stopRequestCodes.indexOf(requestCode);
                AutoCompleteTextView stopAutoComplete = (AutoCompleteTextView) stopsContainer.getChildAt(stopIndex);
                stopAutoComplete.setText(selectedAddress);

                if (stopIndex < stopLatitudes.size()) {
                    stopLatitudes.set(stopIndex, latitude);
                    stopLongitudes.set(stopIndex, longitude);
                } else {
                    stopLatitudes.add(latitude);
                    stopLongitudes.add(longitude);
                }
            }
        }
    }

    private void showLoadingFragment() {
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, new LoadingFragment(), "LoadingFragment")
                .commitAllowingStateLoss();
    }

    private void hideLoadingFragment() {
        Fragment loadingFragment = fragmentManager.findFragmentByTag("LoadingFragment");
        if (loadingFragment != null) {
            fragmentManager.beginTransaction()
                    .remove(loadingFragment)
                    .commitAllowingStateLoss();
        }
    }
    private boolean validateInput() {
        String startPoint = startPointAutoComplete.getText().toString().trim();
        String endPoint = endPointAutoComplete.getText().toString().trim();
        String dateTime = dateTimeEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        String vehicle = vehicleSpinner.getSelectedItem() != null ? vehicleSpinner.getSelectedItem().toString() : "";
        int passengers = passengersSpinner.getSelectedItemPosition();

        if (TextUtils.isEmpty(startPoint)) {
            startPointAutoComplete.setError("Please enter a starting point");
            return false;
        }
        if (TextUtils.isEmpty(endPoint)) {
            endPointAutoComplete.setError("Please enter an endpoint");
            return false;
        }
        if (TextUtils.isEmpty(dateTime)) {
            dateTimeEditText.setError("Please select date and time");
            return false;
        }
        if (TextUtils.isEmpty(price)) {
            priceEditText.setError("Please enter a price");
            return false;
        }
        if (TextUtils.isEmpty(vehicle)) {
            AndroidUtil.showToast(getContext(), "Please select a vehicle");
            return false;
        }
        if (passengers == 0) { // Assuming the first item in the spinner is "Select number of passengers"
            AndroidUtil.showToast(getContext(), "Please select the number of passengers");
            return false;
        }
        return true;
    }

    private void publishRide() {
        if (validateInput()) {

            // Check the number of rides the driver has already published
            mDatabase.child("Rides").orderByChild("driverId").equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int pendingRidesCount = 0;

                    // Count the number of rides with status "Pending"
                    for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                        String rideStatus = rideSnapshot.child("status").getValue(String.class);
                        if ("Pending".equals(rideStatus)) {
                            pendingRidesCount++;
                        }
                    }

                    if (pendingRidesCount >= 3) {
                        AndroidUtil.showToast(getContext(), "You have reached the maximum limit of 3 pending rides.");
                    } else {
                        // If the driver has less than 3 pending rides, proceed with publishing the new ride
                        publishNewRide();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    AndroidUtil.showToast(getContext(), "Failed to check ride limit: " + databaseError.getMessage());
                }
            });
        }
    }

    private void publishNewRide() {
        String startPoint = startPointAutoComplete.getText().toString().trim();
        String endPoint = endPointAutoComplete.getText().toString().trim();
        String dateTime = dateTimeEditText.getText().toString().trim();
        String price = priceEditText.getText().toString().trim();
        long lPrice = Long.parseLong(price);
        String vehicleId = selectedVehicleId;
        int passengers = passengersSpinner.getSelectedItemPosition();

        List<Ride.Stop> stops = new ArrayList<>();
        for (int i = 0; i < stopsContainer.getChildCount(); i++) {
            EditText stopEditText = (EditText) stopsContainer.getChildAt(i);
            String address = stopEditText.getText().toString().trim();
            stops.add(new Ride.Stop(address, stopLatitudes.get(i), stopLongitudes.get(i)));
//                String stop = stopEditText.getText().toString().trim();
//                if (!TextUtils.isEmpty(stop)) {
//                    stops.add(stop);
//                }
        }

        Ride ride = new Ride(startPoint, startPointLat, startPointLng, endPoint, endPointLat, endPointLng, driverId, lPrice, passengers, stops, vehicleId, dateTime, "Pending");
        Log.d("Ride123", ride.toString());
        DatabaseReference rideRef = mDatabase.child("Rides").push();
        rideRef.setValue(ride)
                .addOnSuccessListener(aVoid -> {
                    AndroidUtil.showToast(getContext(), "Ride published successfully!");
                    clearInputs();
                    FirebaseMessaging.getInstance().subscribeToTopic("driver_" + driverId)
                            .addOnCompleteListener(task -> {
                                String msg = "Subscription to driver_" + driverId + " successful";
                                if (!task.isSuccessful()) {
                                    msg = "Subscription failed";
                                }
                                Log.d("FCM", msg);
//                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                            });
                    Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    AndroidUtil.showToast(getContext(), "Failed to publish ride: " + e.getMessage());
                });
    }

    private void clearInputs() {
        startPointAutoComplete.setText("");
        endPointAutoComplete.setText("");
        dateTimeEditText.setText("");
        priceEditText.setText("");
        stopsContainer.removeAllViews();
        passengersSpinner.setSelection(0); // Reset the spinner to default position
    }

    private void checkDriverVerificationStatus() {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverId);

        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isVerified = snapshot.child("verified").getValue(Boolean.class);

                if (isVerified != null && isVerified) {
                    publishRide();
                } else {
                    showVerificationErrorDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void showVerificationErrorDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Verification Required")
                .setMessage("You cannot publish a ride until your account is verified.")
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showDriverRegisterDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Driver Register")
                .setMessage("Do you want to become a driver?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getActivity(), PublishFragment.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null) // Dismiss the dialog if "No" is clicked
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void checkDriverStatus() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("Users").child("Drivers").child(driverId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Check if the user is a driver
                if (snapshot.exists()) {
                    // User is a driver, check if they are verified
                    Boolean isVerified = snapshot.child("verified").getValue(Boolean.class);

                    if (isVerified != null && isVerified) {
                        publishRide();
                    } else {
                        showVerificationErrorDialog();
                    }
                } else {
                    // User is not a driver, prompt them to register
                    showDriverRegisterDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}
