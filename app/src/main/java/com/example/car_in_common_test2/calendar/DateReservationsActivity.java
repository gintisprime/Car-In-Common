package com.example.car_in_common_test2.calendar;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.car_in_common_test2.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DateReservationsActivity extends AppCompatActivity {

    private TextView dateTextView;
    private RecyclerView recyclerView;
    private ArrayList<Reservation> reservations;
    private DatabaseReference databaseReference;
    private ReservationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_reservations);

        dateTextView = findViewById(R.id.dateTextView);  // Correct reference
        recyclerView = findViewById(R.id.recyclerView);  // Correct reference

        String selectedDate = getIntent().getStringExtra("selectedDate");
        dateTextView.setText("Reservations for " + selectedDate);

        reservations = new ArrayList<>();

        // Pass a no-op Runnable since no calendar updates are needed
        adapter = new ReservationAdapter(reservations, () -> {});

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("reservations");
        fetchReservations(selectedDate);
    }


    private void fetchReservations(String date) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reservations.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Reservation reservation = data.getValue(Reservation.class);
                    if (reservation != null && reservation.getDate().equals(date)) {
                        reservations.add(reservation);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle errors
            }
        });
    }
}