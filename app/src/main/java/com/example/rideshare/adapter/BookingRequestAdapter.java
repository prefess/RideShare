package com.example.rideshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Booking;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class BookingRequestAdapter extends RecyclerView.Adapter<BookingRequestAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private Context context;
    private OnBookingActionListener listener;

    public BookingRequestAdapter(Context context, List<Booking> bookingList, OnBookingActionListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        holder.bookingDetails.setText(booking.getBookedSeats() + " seat(s) requested");
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(booking.getCustomerId());
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.bookingName.setText(snapshot.child("name").getValue(String.class));
                    if (snapshot.child("profileImageUrl").exists()) {
                        String profilePictureUrl = snapshot.child("profileImageUrl").getValue(String.class);
                        Glide.with(context).load(profilePictureUrl).into(holder.profileImage);
                    } else {
                        holder.profileImage.setImageResource(R.drawable.ic_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        holder.acceptButton.setOnClickListener(view -> listener.onAccept(booking));
        holder.declineButton.setOnClickListener(view -> listener.onDecline(booking));
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView bookingName, bookingDetails;
        Button acceptButton, declineButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            bookingName = itemView.findViewById(R.id.booking_name);
            bookingDetails = itemView.findViewById(R.id.booking_details);
            acceptButton = itemView.findViewById(R.id.accept_button);
            declineButton = itemView.findViewById(R.id.decline_button);
        }
    }

    public interface OnBookingActionListener {
        void onAccept(Booking booking);
        void onDecline(Booking booking);
    }
}