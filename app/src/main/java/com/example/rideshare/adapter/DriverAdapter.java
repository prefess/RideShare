package com.example.rideshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Driver;
import com.example.rideshare.entity.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.DriverViewHolder> {

    private Context context;
    private List<Driver> driverList;
    private DatabaseReference driversRef;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Driver driver);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public DriverAdapter(Context context, List<Driver> driverList) {
        this.context = context;
        this.driverList = driverList;
        this.driversRef = FirebaseDatabase.getInstance().getReference("Users").child("Drivers");
    }

    @NonNull
    @Override
    public DriverViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver, parent, false);
        return new DriverViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DriverViewHolder holder, int position) {
        Driver driver = driverList.get(position);
        // Bind driver data to the view (profile picture, name, email, phone number)
        holder.driverName.setText(driver.getName());
        holder.driverEmail.setText(driver.getEmail());
        holder.driverPhoneNumber.setText(driver.getPhoneNumber());
        Glide.with(holder.driverProfileImage.getContext()).load(driver.getProfilePicture()).into(holder.driverProfileImage);

////        // Fetch and set driver name
////        driversRef.child(ride.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                if (snapshot.exists()) {
////                    String driverName = snapshot.child("name").getValue(String.class);
////                    holder.driverName.setText(driverName);
////
////                    // Optional: Fetch and set other details like profile picture
////                    String profilePictureUrl = snapshot.child("profilePicture").getValue(String.class);
////                    if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
////                        Glide.with(context)
////                                .load(profilePictureUrl)
////                                .circleCrop() // This will make the image circular
////                                .into(holder.driverProfileImage);
////                    }
////                }
////            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Handle possible errors
//            }
//        });
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(driver);
            }
        });
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class DriverViewHolder extends RecyclerView.ViewHolder {
        // View references (e.g., TextViews, ImageView for profile picture)
        TextView driverName, driverEmail, driverPhoneNumber;
        ImageView driverProfileImage;


        public DriverViewHolder(@NonNull View itemView) {
            super(itemView);
            driverName = itemView.findViewById(R.id.driverName);
            driverEmail = itemView.findViewById(R.id.driverEmail);
            driverPhoneNumber = itemView.findViewById(R.id.driverPhoneNumber);
            driverProfileImage = itemView.findViewById(R.id.driverProfileImage);
        }
    }
}