package com.example.rideshare.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.rideshare.R;
import com.example.rideshare.adapter.BookedRideAdapter;
import com.example.rideshare.entity.Booking;
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


public class BookedRideFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookedRideAdapter adapter;
    private List<Ride> rideList;
    private Spinner filterSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booked_ride, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewBookedRides);
        filterSpinner = view.findViewById(R.id.filter_spinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideList = new ArrayList<>();
        adapter = new BookedRideAdapter(getContext(), rideList);  // Reusing the same adapter
        recyclerView.setAdapter(adapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                filterRides(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        fetchBookedRides();
    }

    private void fetchBookedRides() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user's ID
        DatabaseReference bookingsRef  = FirebaseDatabase.getInstance().getReference().child("Bookings");

        bookingsRef.orderByChild("customerId").equalTo(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rideList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Booking booking = snapshot.getValue(Booking.class);
                    if (booking != null && !booking.getStatus().equals("canceled") && !booking.getStatus().equals("rideCanceled")) {
                        fetchRideDetails(booking.getRideId());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void fetchRideDetails(String rideId) {
        DatabaseReference rideRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(rideId);
        rideRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Ride ride = dataSnapshot.getValue(Ride.class);
                if (ride != null) {
                    ride.setRideId(dataSnapshot.getKey());
                    rideList.add(ride);
                    // Sort the list by date in descending order
                    Collections.sort(rideList, (ride1, ride2) -> Long.compare(convertDateToTimestamp(ride2.getDate()), convertDateToTimestamp(ride1.getDate())));
                    filterRides(0);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void filterRides(int filterOption) {
        List<Ride> filteredList = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Ride ride : rideList) {
            String stringDate = ride.getDate();
            long rideTime = convertDateToTimestamp(stringDate)+3600; // Convert ride.getDate() to a timestamp

            if (filterOption == 0 && rideTime > (currentTime)) {
                filteredList.add(ride); // Upcoming Rides
            } else if (filterOption == 1 && rideTime <= (currentTime)) {
                filteredList.add(ride); // Past Rides
            }
        }

        adapter.updateRides(filteredList);
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