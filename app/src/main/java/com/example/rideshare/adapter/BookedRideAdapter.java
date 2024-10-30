package com.example.rideshare.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rideshare.R;
import com.example.rideshare.activity.BookedRideDetailActivity;
import com.example.rideshare.entity.Ride;

import java.util.List;

public class BookedRideAdapter extends RecyclerView.Adapter<BookedRideAdapter.ViewHolder> {

    private Context context;
    private List<Ride> rideList;
    private OnItemClickListener onItemClickListener;

    public BookedRideAdapter(Context context, List<Ride> rideList) {
        this.context = context;
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booked_ride, parent, false);
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
        // Bind your ride data to the views here

        holder.itemView.setOnClickListener(v -> {
            // Handle item click
            Intent intent = new Intent(context, BookedRideDetailActivity.class);
            intent.putExtra("ride", ride);
            context.startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            // Handle item long click if needed
            return true;
        });
    }

    @Override
    public int getItemCount()
    {
        return rideList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Ride ride);
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
        this.rideList = updatedRides;
        notifyDataSetChanged();
    }
}
