package com.example.car_in_common_test2.calendar;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class FirebaseHelper {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveReservationToFirebase(Reservation reservation) {
        db.collection("reservations")
                .add(reservation)
                .addOnSuccessListener(documentReference -> {
                    // Optionally handle success (e.g., show a message to the user)
                    Log.d("FirebaseHelper", "Reservation added with ID: " + documentReference.getId());
                    // Successfully saved the reservation
                })
                .addOnFailureListener(e -> {
                    // Handle failure (e.g., show an error message)
                    Log.w("FirebaseHelper", "Error adding reservation", e);
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

    public interface FirebaseCallback {
        void onSuccess(List<Reservation> reservations);

        void onFailure(Exception e);
    }
}