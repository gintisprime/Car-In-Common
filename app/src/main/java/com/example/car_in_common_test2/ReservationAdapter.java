package com.example.car_in_common_test2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private ArrayList<Reservation> reservations;

    public ReservationAdapter(ArrayList<Reservation> reservations) {
        this.reservations = reservations;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservations.get(position);
        holder.reasonTextView.setText(reservation.getReason());
        holder.startTimeTextView.setText(reservation.getStartTime());
        holder.endTimeTextView.setText(reservation.getEndTime());
    }

    @Override
    public int getItemCount() {
        return reservations.size();
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
