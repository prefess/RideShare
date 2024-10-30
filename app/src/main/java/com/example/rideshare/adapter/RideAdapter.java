package com.example.rideshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {
    private Context context;
    private List<Ride> rideList;
    private DatabaseReference driversRef;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Ride ride);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RideAdapter(Context context, List<Ride> rideList) {
        this.context = context;
        this.rideList = rideList;
        this.driversRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers");
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        holder.date.setText(ride.getDate());
        holder.destination.setText(ride.getDestination());
        holder.origin.setText(ride.getOrigin());
        holder.price.setText(String.format("VND %s", ride.getPrice()));

        // Fetch and set driver name
        driversRef.child(ride.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String driverName = snapshot.child("name").getValue(String.class);
                    holder.driverName.setText(driverName);

                    // Optional: Fetch and set other details like profile picture
                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);
                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                        Glide.with(context)
                                .load(profilePictureUrl)
                                .circleCrop() // This will make the image circular
                                .into(holder.driverProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(ride);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView driverName, price, date, destination, origin;
        ImageView driverProfileImage;

        public RideViewHolder(View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.driver_name);
            price = itemView.findViewById(R.id.price);
            date = itemView.findViewById(R.id.date);
            origin = itemView.findViewById(R.id.origin);
            destination = itemView.findViewById(R.id.destination);
            driverProfileImage = itemView.findViewById(R.id.profile_picture);
        }
    }
}

