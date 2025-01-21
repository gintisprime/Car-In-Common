package com.example.car_in_common_test2;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirebaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Save a reservation to Firebase after checking for time conflicts.
     *
     * @param date           The date of the reservation.
     * @param startTime      The start time of the reservation.
     * @param endTime        The end time of the reservation.
     * @param newReservation The new reservation object.
     * @param callback       Callback for conflict checking and saving status.
     */
    public static void checkForConflictsAndSave(
            String date,
            String startTime,
            String endTime,
            Reservation newReservation,
            OnConflictCheckCallback callback) {

        db.collection("reservations")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean conflictFound = queryDocumentSnapshots.toObjects(Reservation.class)
                            .stream()
                            .anyMatch(existingReservation -> isTimeConflict(
                                    existingReservation.getStartTime(),
                                    existingReservation.getEndTime(),
                                    startTime,
                                    endTime
                            ));

                    if (conflictFound) {
                        callback.onConflict();
                    } else {
                        saveReservation(newReservation, callback);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Fetch all reservations from Firebase.
     *
     * @param callback Callback for handling fetched reservations or errors.
     */
    public static void fetchAllReservations(FirebaseCallback callback) {
        db.collection("reservations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reservation> reservations = queryDocumentSnapshots.toObjects(Reservation.class);
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Save a reservation directly to Firebase.
     *
     * @param reservation The reservation object to save.
     * @param callback    Callback for handling success or failure.
     */
    private static void saveReservation(Reservation reservation, OnConflictCheckCallback callback) {
        db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Check if there is a time conflict between two reservations.
     *
     * @param existingStart The start time of the existing reservation.
     * @param existingEnd   The end time of the existing reservation.
     * @param newStart      The start time of the new reservation.
     * @param newEnd        The end time of the new reservation.
     * @return True if a conflict exists, false otherwise.
     */
    private static boolean isTimeConflict(String existingStart, String existingEnd, String newStart, String newEnd) {
        return !(existingEnd.compareTo(newStart) <= 0 || existingStart.compareTo(newEnd) >= 0);
    }

    /**
     * Callback interface for conflict checking.
     */
    public interface OnConflictCheckCallback {
        void onConflict();
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * Callback interface for fetching reservations.
     */
    public interface FirebaseCallback {
        void onSuccess(List<Reservation> reservations);
        void onFailure(Exception e);
    }
}
