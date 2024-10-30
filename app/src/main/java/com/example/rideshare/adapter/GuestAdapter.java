package com.example.rideshare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.entity.Booking;
import com.example.rideshare.entity.User;

import java.util.List;

public class GuestAdapter  extends RecyclerView.Adapter<GuestAdapter.ViewHolder> {

    private List<User> customerList;
    private  List<Booking> bookingList;

    public GuestAdapter(List<User> customerList, List<Booking> bookingList) {
        this.customerList = customerList;
        this.bookingList = bookingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User customer = customerList.get(position);
        Booking booking = bookingList.get(position);
        holder.guestName.setText(customer.name);
        holder.guestEmail.setText(customer.email);
        holder.guestPhone.setText(customer.phoneNumber);
        holder.guestSeatBooked.setText(booking.getBookedSeats() + " seat(s) booked");
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView guestName, guestEmail, guestPhone, guestSeatBooked;

        public ViewHolder(View itemView) {
            super(itemView);
            guestName = itemView.findViewById(R.id.guest_name);
            guestEmail = itemView.findViewById(R.id.guest_email);
            guestPhone = itemView.findViewById(R.id.guest_phone);
            guestSeatBooked = itemView.findViewById(R.id.guest_seat_booked);
        }
    }
}