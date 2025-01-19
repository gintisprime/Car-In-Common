package com.example.car_in_common_test2.calendar;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveReservationToFirebase(Reservation reservation) {
        db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirebaseHelper", "Reservation added: " + reservation.toString());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error adding reservation", e);
                });
    }

    public static void fetchReservationsForDate(String date, FirebaseCallback callback) {
        db.collection("reservations")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reservation> reservations = queryDocumentSnapshots.toObjects(Reservation.class);
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(callback::onFailure);
    }
    public static void checkForConflictsAndSave(
            String date, String newStartTime, String newEndTime, Reservation newReservation,
            OnConflictCheckCallback callback) {

        db.collection("reservations")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean conflictFound = false;

                    for (Reservation existingReservation : queryDocumentSnapshots.toObjects(Reservation.class)) {
                        if (timeOverlaps(existingReservation.getStartTime(), existingReservation.getEndTime(), newStartTime, newEndTime)) {
                            conflictFound = true;
                            break;
                        }
                    }

                    if (conflictFound) {
                        callback.onConflict();
                    } else {
                        saveReservationToFirebase(newReservation);
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    private static boolean timeOverlaps(String existingStart, String existingEnd, String newStart, String newEnd) {
        // Parse time strings into Date objects
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date existingStartTime = timeFormat.parse(existingStart);
            Date existingEndTime = timeFormat.parse(existingEnd);
            Date newStartTime = timeFormat.parse(newStart);
            Date newEndTime = timeFormat.parse(newEnd);

            return (newStartTime.before(existingEndTime) && newEndTime.after(existingStartTime));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface OnConflictCheckCallback {
        void onConflict();
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface FirebaseCallback {
        void onSuccess(List<Reservation> reservations);
        void onFailure(Exception e);
    }
}
