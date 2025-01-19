package com.example.car_in_common_test2.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.car_in_common_test2.R;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private final List<Reservation> reservationList;

    public ReservationAdapter(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        // Bind data to TextViews
        holder.startTimeTextView.setText("Start Time: " + reservation.getStartTime());
        holder.endTimeTextView.setText("End Time: " + reservation.getEndTime());

        // Check if the reservation is emergency
        if (reservation.isEmergency()) {
            holder.reasonTextView.setVisibility(View.GONE); // Hide reason for emergency reservations
            holder.typeTextView.setText("Type: Emergency");
        } else {
            holder.reasonTextView.setVisibility(View.VISIBLE); // Show reason for normal reservations
            holder.reasonTextView.setText("Reason: " + reservation.getReason());
            holder.typeTextView.setText("Type: Normal");
        }
    }



    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView reasonTextView, startTimeTextView, endTimeTextView, typeTextView;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
        }
    }
}
