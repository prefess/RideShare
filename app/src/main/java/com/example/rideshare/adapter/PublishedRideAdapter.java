package com.example.rideshare.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.entity.Booking;
import com.example.rideshare.entity.Ride;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PublishedRideAdapter extends RecyclerView.Adapter<PublishedRideAdapter.ViewHolder> {

    private Context context;
    private List<Ride> rideList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(Ride ride);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public PublishedRideAdapter(Context context, List<Ride> rideList) {
        this.context = context;
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_published_ride, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        holder.tvRideNumber.setText("Ride No. #" + ride.getRideId());
        holder.tvStartLocation.setText(ride.getOrigin());
        holder.tvStartTime.setText(ride.getDate());
        holder.tvEndLocation.setText(ride.getDestination());
//        holder.tvEndTime.setText(ride.getEndTime());
        holder.tvDate.setText(ride.getDate());
        holder.tvPrice.setText(String.format("VND %d ", ride.getPrice()));
//        holder.tvStatus.setText("You have a " + ride.getRequests().size() + " request.");
        // Check if the ride has any requests

        DatabaseReference ridesRef = FirebaseDatabase.getInstance().getReference().child("Rides").child(ride.getRideId()).child("bookings");
        ridesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot bookSnap : snapshot.getChildren()) {
                    Booking booking = bookSnap.getValue(Booking.class);
                    if (booking != null && booking.getStatus().equals("pending")) {
                        // Show the "Pending request" status
                        holder.tvStatus.setVisibility(View.VISIBLE);
                        holder.tvStatus.setText("You have a Pending request.");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(ride);
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return rideList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvRideNumber, tvStartLocation, tvStartTime, tvEndLocation, tvEndTime, tvDate, tvPrice, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRideNumber = itemView.findViewById(R.id.tvRideNumber);
            tvStartLocation = itemView.findViewById(R.id.tvStartLocation);
            tvStartTime = itemView.findViewById(R.id.tvStartTime);
            tvEndLocation = itemView.findViewById(R.id.tvEndLocation);
            tvEndTime = itemView.findViewById(R.id.tvEndTime);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (onItemClickListener != null && position != RecyclerView.NO_POSITION) {
                onItemClickListener.onItemClick(rideList.get(position));
            }
        }
    }

    public void updateRides(List<Ride> updatedRides) {
        this.rideList.clear();
//        this.rideList = updatedRides;
        this.rideList.addAll(updatedRides);
        notifyDataSetChanged();

    }
}
