package com.example.car_in_common_test2;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView selectedDateTextView;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter adapter;
    private ArrayList<Reservation> reservations;
    private ImageButton addReservationButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
        addReservationButton = findViewById(R.id.addReservationButton);

        reservations = new ArrayList<>();
        adapter = new ReservationAdapter(reservations);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("reservations");

        // Set listener for date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            selectedDateTextView.setText("Reservations for " + selectedDate);
            fetchReservations(selectedDate);
        });

        addReservationButton.setOnClickListener(v -> {
            // Show dialog for two choices
            new AlertDialog.Builder(CalendarActivity.this)
                    .setTitle("Choose Reservation Type")
                    .setItems(new String[]{"Normal Reservation", "Emergency Reservation"}, (dialog, which) -> {
                        if (which == 0) {
                            // Open Normal Reservation Fragment
                            NormalReservationFragment fragment = new NormalReservationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("selectedDate", selectedDateTextView.getText().toString().replace("Reservations for ", "").trim());
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), "NormalReservationFragment");
                        } else if (which == 1) {
                            // Open Emergency Reservation Fragment
                            EmergencyReservationFragment fragment = new EmergencyReservationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("selectedDate", selectedDateTextView.getText().toString().replace("Reservations for ", "").trim());
                            fragment.setArguments(bundle);
                            fragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
                        }
                    })
                    .show();
        });
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
