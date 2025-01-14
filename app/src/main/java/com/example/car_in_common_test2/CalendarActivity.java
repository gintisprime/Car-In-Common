package com.example.car_in_common_test2;

import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
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

        // Handle calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            selectedDateTextView.setText("Reservations for " + selectedDate);
            fetchReservations(selectedDate);  // Refresh reservations on the calendar view
        });


        // Handle button to add a reservation
        addReservationButton.setOnClickListener(v -> showReservationTypeDialog());
    }

    // Show dialog to choose between normal or emergency reservation
    private void showReservationTypeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Choose Reservation Type")
                .setItems(new String[]{"Normal Reservation", "Emergency Reservation"}, (dialog, which) -> {
                    String selectedDate = selectedDateTextView.getText().toString().replace("Reservations for ", "").trim();
                    if (which == 0) {
                        NormalReservationFragment fragment = new NormalReservationFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedDate", selectedDate);  // Pass selected date to fragment
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "NormalReservationFragment");
                    } else if (which == 1) {
                        EmergencyReservationFragment fragment = new EmergencyReservationFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedDate", selectedDate);  // Pass selected date to fragment
                        fragment.setArguments(bundle);
                        fragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
                    }
                })
                .show();
    }

    public void refreshReservations(String selectedDate) {
        // Update the selected date TextView to show the current date
        selectedDateTextView.setText("Reservations for " + selectedDate);

        // Call the method to fetch the updated reservations from Firebase for the selected date
        fetchReservations(selectedDate);
    }


    // Fetch reservations from Firebase based on the selected date
    private void fetchReservations(String date) {
        databaseReference.child(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                reservations.clear();  // Clear the current list of reservations
                for (DataSnapshot data : snapshot.getChildren()) {
                    Reservation reservation = data.getValue(Reservation.class);
                    if (reservation != null) {
                        reservations.add(reservation);  // Add reservation to the list
                    }
                }
                adapter.notifyDataSetChanged();  // Notify adapter that data has changed
                reservationsRecyclerView.setVisibility(reservations.isEmpty() ? View.GONE : View.VISIBLE);  // Show/hide the RecyclerView based on reservations
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CalendarActivity.this, "Failed to load reservations.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
