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

        // Bind Date, Start Time, and End Time
        holder.dateTextView.setText("Ημερομηνία: " + reservation.getDate());
        holder.startTimeTextView.setText("Ώρα Έναρξης: " + reservation.getStartTime());
        holder.endTimeTextView.setText("Ώρα Λήξης: " + reservation.getEndTime());

        // Check Reservation Type
        if (reservation.isEmergency()) {
            holder.reasonTextView.setVisibility(View.GONE);
            holder.releaseTimeCertainTextView.setVisibility(View.GONE);
            holder.typeTextView.setText("Τύπος Δέσμευσης: Επείγουσα");
        } else {
            holder.reasonTextView.setVisibility(View.VISIBLE);
            holder.releaseTimeCertainTextView.setVisibility(View.VISIBLE);
            holder.typeTextView.setText("Τύπος Δέσμευσης: Κανονική");

            // Bind Release Time Certain
            String releaseTimeCertainText = reservation.isReleaseTimeCertain() ? "Ναι" : "Όχι";
            holder.releaseTimeCertainTextView.setVisibility(View.VISIBLE);
            holder.releaseTimeCertainTextView.setText("Σίγουρη ώρα αποδέσμευσης του οχήματος?: " + releaseTimeCertainText);
        }
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, startTimeTextView, endTimeTextView, typeTextView, reasonTextView, releaseTimeCertainTextView;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            releaseTimeCertainTextView = itemView.findViewById(R.id.releaseTimeCertainTextView);
        }
    }
}
