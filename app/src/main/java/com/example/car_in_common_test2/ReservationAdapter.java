package com.example.car_in_common_test2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;  // Correctly import List

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservations;  // Use List instead of ArrayList directly

    public ReservationAdapter(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public ReservationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.reasonTextView.setText(reservation.getReason());
        holder.startTimeTextView.setText(reservation.getStartTime());
        holder.endTimeTextView.setText(reservation.getEndTime());
    }

    @Override
    public int getItemCount() {
        return reservations.size();  // Returns the size of the list
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {

        TextView reasonTextView;
        TextView startTimeTextView;
        TextView endTimeTextView;

        public ReservationViewHolder(View itemView) {
            super(itemView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
        }
    }
}
