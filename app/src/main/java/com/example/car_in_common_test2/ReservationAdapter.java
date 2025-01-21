package com.example.car_in_common_test2;

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

        if (reservation.isEmergency()) {
            holder.reasonTextView.setText("Type: Emergency");
        } else {
            holder.reasonTextView.setText("Reason: " + reservation.getReason());
        }

        holder.startTimeTextView.setText("Start Time: " + reservation.getStartTime());
        holder.endTimeTextView.setText("End Time: " + reservation.getEndTime());

        String importanceText = reservation.isImportant() ? "Important" : "Not Important";
        holder.importanceTextView.setText("Importance: " + importanceText);
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public void updateReservations(List<Reservation> filteredReservations) {
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView reasonTextView, startTimeTextView, endTimeTextView, importanceTextView;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            importanceTextView = itemView.findViewById(R.id.impotanceView);
        }
    }
}
