package com.example.car_in_common_test2;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private String selectedDate;
    private Set<String> reservedDates = new HashSet<>();
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        ImageButton addReservationButton = findViewById(R.id.addReservationButton);
        TextView selectedDateTextView = findViewById(R.id.selectedDateTextView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);

        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(reservationList);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(reservationAdapter);

        // Set default selected date to today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        selectedDateTextView.setText("Δεσμεύσεις Ημέρας: " + selectedDate);

        // Fetch reservations from Firebase
        fetchReservations();

        // Handle date selection
        calendarView.setOnDayClickListener(eventDay -> {
            Date clickedDate = eventDay.getCalendar().getTime();
            selectedDate = sdf.format(clickedDate);
            selectedDateTextView.setText("Δεσμεύσεις Ημέρας: " + selectedDate);
            showReservationsForDate();
        });

        // Show options for normal or emergency reservation
        addReservationButton.setOnClickListener(v -> showReservationOptions());
    }

    private void fetchReservations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            if (date != null) {
                                reservedDates.add(date);
                            }
                        }
                        updateCalendarWithReservations();
                    } else {
                        Toast.makeText(CalendarActivity.this, "Error fetching reservations.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCalendarWithReservations() {
        List<EventDay> events = new ArrayList<>();
        for (String date : reservedDates) {
            try {
                // Convert string date to Date object
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date eventDate = sdf.parse(date);

                if (eventDate != null) {
                    // Convert Date to Calendar
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(eventDate);

                    // Get the Drawable resource (e.g., event_marker.xml or event_marker.png)
                    Drawable eventMarker = ContextCompat.getDrawable(this, R.drawable.event_marker);

                    // Add the event to the list
                    if (eventMarker != null) {
                        events.add(new EventDay(calendar, eventMarker));  // Use Calendar and Drawable
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        calendarView.setEvents(events);
    }

    private void showReservationsForDate() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reservations")
                .whereEqualTo("date", selectedDate)  // Filter by selected date
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reservationList.clear(); // Clear the current list before adding new data
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String reason = document.getString("reason");
                            String startTime = document.getString("startTime");
                            String endTime = document.getString("endTime");

                            if (reason != null && startTime != null && endTime != null) {
                                reservationList.add(new Reservation(reason, startTime, endTime)); // Add to list
                            }
                        }
                        reservationAdapter.notifyDataSetChanged(); // Notify the adapter to update the UI
                    } else {
                        Toast.makeText(CalendarActivity.this, "Error fetching reservations.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showReservationOptions() {
        // Dialog for choosing reservation type
        ReservationTypeDialogFragment dialog = new ReservationTypeDialogFragment();
        dialog.setSelectedDate(selectedDate);
        dialog.show(getSupportFragmentManager(), "ReservationTypeDialogFragment");
    }
}
