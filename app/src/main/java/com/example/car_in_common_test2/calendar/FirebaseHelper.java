package com.example.car_in_common_test2.calendar;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

    private static final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("reservations");
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Check if user is authenticated
    public static boolean isAuthenticated() {
        return auth.getCurrentUser() != null;
    }

    public static void saveReservationToFirebase(Reservation reservation, FirebaseCallback callback) {
        if (!isAuthenticated()) {
            callback.onFailure(new Exception("User is not authenticated."));
            return;
        }

        String key = dbRef.push().getKey();
        if (key == null) {
            callback.onFailure(new Exception("Failed to generate Firebase key"));
            return;
        }

        Map<String, Object> reservationData = new HashMap<>();
        reservationData.put("id", key);
        reservationData.put("reason", reservation.getReason());
        reservationData.put("startTime", reservation.getStartTime());
        reservationData.put("endTime", reservation.getEndTime());
        reservationData.put("date", reservation.getDate());
        reservationData.put("isEmergency", reservation.isEmergency());
        reservationData.put("releaseTimeCertain", reservation.isReleaseTimeCertain());

        dbRef.child(key)
                .setValue(reservationData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public static void fetchReservationsForDate(String date, FirebaseCallback callback) {
        if (!isAuthenticated()) {
            callback.onFailure(new Exception("User is not authenticated."));
            return;
        }

        dbRef.orderByChild("date").equalTo(date)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Reservation reservation = data.getValue(Reservation.class);
                            if (reservation != null) {
                                reservation.setId(data.getKey());
                                reservations.add(reservation);
                            }
                        }
                        callback.onSuccess(reservations);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.toException());
                    }
                });
    }

    public static void fetchAllReservations(FirebaseCallback callback) {
        if (!isAuthenticated()) {
            callback.onFailure(new Exception("User is not authenticated."));
            return;
        }

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Reservation> reservations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Reservation reservation = data.getValue(Reservation.class);
                    if (reservation != null) {
                        reservations.add(reservation);
                    }
                }
                callback.onSuccess(reservations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }

    public static void deleteReservationFromFirebase(String reservationId, FirebaseCallback callback) {
        if (!isAuthenticated()) {
            callback.onFailure(new Exception("User is not authenticated."));
            return;
        }

        dbRef.child(reservationId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public interface FirebaseCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }
}
