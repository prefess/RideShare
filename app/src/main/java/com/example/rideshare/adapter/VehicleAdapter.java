package com.example.rideshare.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rideshare.R;
import com.example.rideshare.entity.Vehicle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicleList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    public VehicleAdapter(List<Vehicle> vehicleList, String driverId) {
        this.vehicleList = vehicleList;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users")
                .child("Drivers")
                .child(driverId)
                .child("Vehicles");
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.modelTextView.setText(vehicle.getVehicleType());
        Glide.with(holder.itemView.getContext()).load(vehicle.getVehicleImageUrl()).into(holder.imageView);

        holder.deleteView.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Vehicle")
                    .setMessage("Are you sure you want to delete?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String vehicleId = vehicle.getVehicleId();  // Assuming you have a unique ID for each vehicle.
                            mDatabase.child(vehicleId).child("status").setValue("disabled");
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView, deleteView;
        TextView modelTextView;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            deleteView = itemView.findViewById(R.id.vehicle_delete);
            imageView = itemView.findViewById(R.id.vehicle_image);
            modelTextView = itemView.findViewById(R.id.vehicle_model);
        }
    }
}