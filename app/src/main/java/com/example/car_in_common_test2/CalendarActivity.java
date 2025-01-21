package com.example.car_in_common_test2;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.car_in_common_test2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends MainMenuActivity {

    private CalendarView calendarView;
    private String selectedDate;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("CalendarActivity", "CalendarActivity is created.");
        setContentView(R.layout.activity_calendar);
        // Inflate layout into BaseActivity's content frame
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.contentFrame), true);

        // Initialize UI components
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
        selectedDateTextView.setText("Reservations for: " + selectedDate);

        // Fetch and display reservations
        fetchAllReservations();

        // Handle date selection
        calendarView.setOnDayClickListener(eventDay -> {
            Date clickedDate = eventDay.getCalendar().getTime();
            selectedDate = sdf.format(clickedDate);
            selectedDateTextView.setText("Reservations for: " + selectedDate);
            fetchReservationsForSelectedDate();
        });

        // Add a new reservation
        addReservationButton.setOnClickListener(v -> showReservationOptions());
    }

    private void fetchAllReservations() {
        FirebaseHelper.fetchAllReservations(new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                reservationList.clear();
                reservationList.addAll(reservations);

                // Update RecyclerView for the selected date
                fetchReservationsForSelectedDate();

                // Add events to the calendar view
                List<EventDay> events = mapReservationsToEvents(reservations);
                calendarView.setEvents(events);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CalendarActivity", "Error fetching reservations", e);
                Toast.makeText(CalendarActivity.this, "Error fetching reservations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchReservationsForSelectedDate() {
        List<Reservation> filteredReservations = new ArrayList<>();
        for (Reservation reservation : reservationList) {
            if (reservation.getDate().equals(selectedDate)) {
                filteredReservations.add(reservation);
            }
        }
        reservationAdapter.updateReservations(filteredReservations);
    }

    private List<EventDay> mapReservationsToEvents(List<Reservation> reservations) {
        List<EventDay> events = new ArrayList<>();

        for (Reservation reservation : reservations) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            try {
                Date date = sdf.parse(reservation.getDate());
                calendar.setTime(date);

                Drawable drawable = ContextCompat.getDrawable(this,
                        reservation.isEmergency() ? R.drawable.red_dot : R.drawable.green_dot);

                if (drawable != null) {
                    events.add(new EventDay(calendar, drawable));
                }
            } catch (Exception e) {
                Log.e("CalendarActivity", "Error parsing date: " + reservation.getDate(), e);
            }
        }

        return events;
    }

    private void showReservationOptions() {
        // Existing dialog to choose Normal or Emergency reservation
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose reservation type");

        String[] options = {"Normal Reservation", "Emergency Reservation"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                NormalReservationFragment normalFragment = new NormalReservationFragment();
                normalFragment.setSelectedDate(selectedDate);
                normalFragment.show(getSupportFragmentManager(), "NormalReservationFragment");
            } else {
                EmergencyReservationFragment emergencyFragment = new EmergencyReservationFragment();
                emergencyFragment.setSelectedDate(selectedDate);
                emergencyFragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
            }
        });

        builder.create().show();
    }
}
