package com.example.car_in_common_test2.calendar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.utils.BaseActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends BaseActivity {

    private static final String TAG = "CalendarActivity";

    private CalendarView calendarView;
    private String selectedDate;
    private RecyclerView reservationsRecyclerView;
    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate layout into BaseActivity's content frame
        getLayoutInflater().inflate(R.layout.activity_calendar, findViewById(R.id.contentFrame), true);

        // Initialize UI components
        initializeUI();

        // Load reservations and calendar events
        loadInitialData();

        // Handle calendar date selection
        calendarView.setOnDayClickListener(eventDay -> {
            selectedDate = formatDate(eventDay.getCalendar().getTime());
            updateSelectedDateText();
            fetchReservations();
        });
    }

    private void initializeUI() {
        calendarView = findViewById(R.id.calendarView);
        ImageButton addReservationButton = findViewById(R.id.addReservationButton);
        TextView selectedDateTextView = findViewById(R.id.selectedDateTextView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);

        // Initialize RecyclerView
        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(reservationList);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(reservationAdapter);

        // Set current date as default
        selectedDate = formatDate(new Date());
        updateSelectedDateText();

        // Add reservation button click listener
        addReservationButton.setOnClickListener(v -> showReservationOptions());
    }

    private void loadInitialData() {
        fetchReservations();
        fetchEventsForCalendar();
    }

    private void fetchReservations() {
        FirebaseHelper.fetchReservationsForDate(selectedDate, new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Reservation> reservations = (List<Reservation>) result;
                    updateReservations(reservations);
                } else {
                    Log.e(TAG, "Unexpected result type in fetchReservations");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching reservations", e);
                Toast.makeText(CalendarActivity.this, "Failed to fetch reservations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchEventsForCalendar() {
        FirebaseHelper.fetchAllReservations(new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(Object result) {
                if (result instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Reservation> reservations = (List<Reservation>) result;
                    updateCalendarEvents(reservations);
                } else {
                    Log.e(TAG, "Unexpected result type in fetchEventsForCalendar");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching calendar events", e);
                Toast.makeText(CalendarActivity.this, "Failed to fetch calendar events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateReservations(List<Reservation> reservations) {
        reservationList.clear();
        reservationList.addAll(sortByDateAndTime(reservations));
        reservationAdapter.notifyDataSetChanged();
    }

    private void updateCalendarEvents(List<Reservation> reservations) {
        List<EventDay> events = new ArrayList<>();
        for (Reservation reservation : reservations) {
            try {
                Date date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(reservation.getDate());
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int dotDrawable = reservation.isEmergency() ? R.drawable.red_dot : R.drawable.green_dot;
                    events.add(new EventDay(calendar, dotDrawable));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing reservation date", e);
            }
        }
        calendarView.setEvents(events);
    }

    private void showReservationOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Reservation Type");
        builder.setItems(new String[]{"Κανονική Δέσμευση", "Δέσμευση έκτακτης ανάγκης"}, (dialog, which) -> {
            if (which == 0) {
                openNormalReservationDialog();
            } else {
                openEmergencyReservationDialog();
            }
        });
        builder.create().show();
    }

    private void openNormalReservationDialog() {
        NormalReservationFragment fragment = new NormalReservationFragment();
        fragment.setSelectedDate(selectedDate);
        fragment.setOnReservationSavedListener(this::onReservationSaved);
        fragment.show(getSupportFragmentManager(), "NormalReservationFragment");
    }

    private void openEmergencyReservationDialog() {
        EmergencyReservationFragment fragment = new EmergencyReservationFragment();
        fragment.setSelectedDate(selectedDate);
        fragment.setOnReservationSavedListener(this::onReservationSaved);
        fragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
    }

    private void onReservationSaved() {
        fetchReservations();
        fetchEventsForCalendar();
    }

    private void updateSelectedDateText() {
        TextView selectedDateTextView = findViewById(R.id.selectedDateTextView);
        selectedDateTextView.setText("Reservations for: " + selectedDate);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date);
    }

    private List<Reservation> sortByDateAndTime(List<Reservation> reservations) {
        reservations.sort((r1, r2) -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                Date date1 = sdf.parse(r1.getDate() + " " + r1.getStartTime());
                Date date2 = sdf.parse(r2.getDate() + " " + r2.getStartTime());
                return date2.compareTo(date1);
            } catch (Exception e) {
                Log.e(TAG, "Error sorting reservations", e);
                return 0;
            }
        });
        return reservations;
    }
}
