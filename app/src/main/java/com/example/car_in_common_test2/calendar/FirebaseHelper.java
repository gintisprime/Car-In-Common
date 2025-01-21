package com.example.car_in_common_test2.calendar;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirebaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Save a reservation to Firestore
    public static void saveReservationToFirebase(Reservation reservation, FirebaseCallback callback) {
        db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirebaseHelper", "Η δέσμευση καταχωρήθηκε: " + documentReference.getId());
                    callback.onSuccess(List.of(reservation));
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Σφάλμα καταχώρησης δέσμευσης", e);
                    callback.onFailure(e);
                });
    }

    // Fetch reservations for a specific date
    public static void fetchReservationsForDate(String date, FirebaseCallback callback) {
        db.collection("reservations")
                .whereEqualTo("date", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reservation> reservations = queryDocumentSnapshots.toObjects(Reservation.class);
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error fetching reservations for date: " + date, e);
                    callback.onFailure(e);
                });
    }

    // Fetch all reservations
    public static void fetchAllReservations(FirebaseCallback callback) {
        db.collection("reservations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reservation> reservations = queryDocumentSnapshots.toObjects(Reservation.class);
                    callback.onSuccess(reservations);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error fetching all reservations", e);
                    callback.onFailure(e);
                });
    }

    public interface FirebaseCallback {
        void onSuccess(List<Reservation> reservations);
        void onFailure(Exception e);
    }
}
