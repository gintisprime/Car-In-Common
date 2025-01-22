package com.example.car_in_common_test2.calendar;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.car_in_common_test2.R;

import java.util.ArrayList;
import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private final List<Reservation> reservationList;

    public ReservationAdapter(List<Reservation> reservationList) {
        this.reservationList = (reservationList != null) ? reservationList : new ArrayList<>();
    }


    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Reservation reservation = reservationList.get(position);

        holder.dateTextView.setText("Ημερομηνία: " + reservation.getDate());
        holder.startTimeTextView.setText("Ώρα Έναρξης: " + reservation.getStartTime());
        holder.endTimeTextView.setText("Ώρα Λήξης: " + reservation.getEndTime());
        holder.typeTextView.setText(reservation.isEmergency() ? "Τύπος Δέσμευσης: Επείγουσα" : "Τύπος Δέσμευσης: Κανονική");

        if (reservation.isEmergency()) {
            holder.reasonTextView.setVisibility(View.GONE);
            holder.releaseTimeCertainTextView.setVisibility(View.GONE);
        } else {
            holder.reasonTextView.setVisibility(View.VISIBLE);
            holder.reasonTextView.setText("Σκοπός: " + reservation.getReason());
            holder.releaseTimeCertainTextView.setVisibility(View.VISIBLE);
            holder.releaseTimeCertainTextView.setText("Σίγουρη ώρα αποδέσμευσης: " +
                    (reservation.isReleaseTimeCertain() ? "Ναι" : "Όχι"));
        }

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseHelper.deleteReservationFromFirebase(reservation.getId(), new FirebaseHelper.FirebaseCallback() {
                @Override
                public void onSuccess(Object result) {
                    reservationList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(holder.itemView.getContext(), "Η δέσμευση διαγράφηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(holder.itemView.getContext(), "Αποτυχία διαγραφής δέσμευσης.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return reservationList != null ? reservationList.size() : 0;
    }

    public static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView, startTimeTextView, endTimeTextView, typeTextView, reasonTextView, releaseTimeCertainTextView;
        ImageButton deleteButton;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            startTimeTextView = itemView.findViewById(R.id.startTimeTextView);
            endTimeTextView = itemView.findViewById(R.id.endTimeTextView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            releaseTimeCertainTextView = itemView.findViewById(R.id.releaseTimeCertainTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
