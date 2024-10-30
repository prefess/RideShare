package com.example.rideshare.fragment;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.rideshare.R;
import com.example.rideshare.activity.DriverRideDetailActivity;
import com.example.rideshare.adapter.PublishedRideAdapter;
import com.example.rideshare.entity.Ride;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PublishedRideFragment extends Fragment {

    private RecyclerView recyclerView;
    private PublishedRideAdapter adapter;
    private List<Ride> rideList;
    private Spinner filterSpinner;
    private Dialog loadingDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_published_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.recyclerViewPublishedRides);
        filterSpinner = view.findViewById(R.id.filter_spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideList = new ArrayList<>();
        adapter = new PublishedRideAdapter(getContext(), rideList);
        recyclerView.setAdapter(adapter);

        // Set up swipe-to-refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Reload your data here
            int position = filterSpinner.getSelectedItemPosition();
            fetchPublishedRides(position);
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                filterRides(position);
                fetchPublishedRides(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Initialize and show loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.setCancelable(false); // Make it non-cancelable so that it doesn't dismiss on back press
        loadingDialog.show();

        fetchPublishedRides(0);

        adapter.setOnItemClickListener(ride -> {
            Intent intent = new Intent(getActivity(), DriverRideDetailActivity.class);
            intent.putExtra("seatsRequest", "4");
            intent.putExtra("ride", ride);
            startActivity(intent);
        });
    }

    private void fetchPublishedRides(int position) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's ID
        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference().child("Rides");
        ridesRef.orderByChild("driverId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rideList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null) {
                        ride.setRideId(snapshot.getKey());
                        rideList.add(ride);
                    }
                }

                // Sort the list by date in descending order
                Collections.sort(rideList, (ride1, ride2) -> Long.compare(convertDateToTimestamp(ride2.getDate()), convertDateToTimestamp(ride1.getDate())));

                filterRides(position);
                recyclerView.setAdapter(null);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();


                loadingDialog.dismiss(); // Ensure it's dismissed after updating
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                // Stop swipe refresh animation in case of error
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void filterRides(int filterOption) {
        List<Ride> filteredList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();



        for (Ride ride : rideList) {

            String stringDate = ride.getDate();
            long rideTime = convertDateToTimestamp(stringDate) + 86400; // Convert ride.getDate() to a timestamp

            String rideStatus = ride.getStatus(); // Assuming there's a getStatus() method in the Ride class

            if (filterOption == 0) {
                // Filter for Upcoming Rides (Pending)
                if ("pending".equalsIgnoreCase(rideStatus) && rideTime > currentTime) {
                    filteredList.add(ride);
                }
            } else if (filterOption == 1) {
                // Filter for Past Rides (Complete and Canceled)
                if (("completed".equalsIgnoreCase(rideStatus) || "canceled".equalsIgnoreCase(rideStatus) || "pending".equalsIgnoreCase(rideStatus)) && rideTime <= currentTime || "completed".equalsIgnoreCase(rideStatus)) {
                    filteredList.add(ride);
                }
            } else if(filterOption == 2) {
                if ("processing".equalsIgnoreCase(rideStatus)) {
                    filteredList.add(ride);
                }
            }

//            String stringDate = ride.getDate();
//
//            long rideTime = convertDateToTimestamp(stringDate); // Convert ride.getDate() to a timestamp
//
//            if (filterOption == 0 && rideTime > currentTime) {
//                filteredList.add(ride); // Upcoming Rides
//            } else if (filterOption == 1 && rideTime <= currentTime) {
//                filteredList.add(ride); // Past Rides
//            }
        }

        adapter.updateRides(filteredList);
        adapter.notifyDataSetChanged(); // Ensure the adapter is notified of the data change
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