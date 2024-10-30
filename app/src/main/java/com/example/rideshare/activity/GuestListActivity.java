package com.example.rideshare.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.adapter.GuestAdapter;
import com.example.rideshare.entity.Booking;
import com.example.rideshare.entity.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GuestListActivity extends AppCompatActivity {

    private RecyclerView guestRecyclerView;
    private GuestAdapter guestAdapter;
    private List<Booking> guestList;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_list);

        guestRecyclerView = findViewById(R.id.guest_recycler_view);
        guestRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference().child("Users").child("Customer");

        // Get the guest list (list of bookings) from the intent
        guestList = (ArrayList<Booking>) getIntent().getSerializableExtra("guest_list");

        // Iterate over the guest list and fetch customer details for each booking
        List<User> customerList = new ArrayList<>();
        List<Booking> bookingList = new ArrayList<>();
        for (Booking booking : guestList) {
            fetchCustomerDetails(booking.getCustomerId(), customer -> {
                customerList.add(customer);
                bookingList.add(booking);
                guestAdapter.notifyDataSetChanged();
            });
        }

        guestAdapter = new GuestAdapter(customerList, bookingList);
        guestRecyclerView.setAdapter(guestAdapter);
    }

    private void fetchCustomerDetails(String customerId, final CustomerCallback callback) {
        usersRef.child(customerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User customer = dataSnapshot.getValue(User.class);
                if (customer != null) {
                    callback.onCustomerFetched(customer);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("GuestListActivity", "Error fetching customer details", databaseError.toException());
            }
        });
    }

    interface CustomerCallback {
        void onCustomerFetched(User customer);
    }
}