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

        calendarView = findViewById(R.id.calendarView);
        ImageButton addReservationButton = findViewById(R.id.addReservationButton);
        TextView selectedDateTextView = findViewById(R.id.selectedDateTextView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);

        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(reservationList);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(reservationAdapter);

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        selectedDateTextView.setText("Reservations for: " + selectedDate);

        fetchReservations();        // Load reservations for today
        fetchEventsForCalendar();   // Load all calendar events

        calendarView.setOnDayClickListener(eventDay -> {
            Date clickedDate = eventDay.getCalendar().getTime();
            selectedDate = sdf.format(clickedDate);
            selectedDateTextView.setText("Reservations for: " + selectedDate);
            fetchReservations();
        });

        addReservationButton.setOnClickListener(v -> showReservationOptions());
    }

    private void fetchReservations() {
        FirebaseHelper.fetchReservationsForDate(selectedDate, new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                reservationList.clear();

                // Sort reservations by date and time (latest first)
                Collections.sort(reservations, (r1, r2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                        Date date1 = sdf.parse(r1.getDate() + " " + r1.getStartTime());
                        Date date2 = sdf.parse(r2.getDate() + " " + r2.getStartTime());
                        return date2.compareTo(date1);
                    } catch (Exception e) {
                        Log.e("CalendarActivity", "Error parsing reservation date", e);
                        return 0;
                    }
                });

                reservationList.addAll(reservations);
                reservationAdapter.notifyDataSetChanged(); // Refresh RecyclerView
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CalendarActivity.this, "Error fetching reservations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void fetchEventsForCalendar() {
        FirebaseHelper.fetchAllReservations(new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                List<EventDay> events = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

                for (Reservation reservation : reservations) {
                    try {
                        Date parsedDate = sdf.parse(reservation.getDate());
                        Calendar calendar = Calendar.getInstance();
                        if (parsedDate != null) {
                            calendar.setTime(parsedDate);
                            int dotDrawable = reservation.isEmergency() ? R.drawable.red_dot : R.drawable.green_dot;
                            events.add(new EventDay(calendar, dotDrawable));
                        }
                    } catch (Exception e) {
                        Log.e("CalendarActivity", "Error parsing reservation date", e);
                    }
                }

                calendarView.setEvents(events);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CalendarActivity.this, "Error fetching calendar events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReservationOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Reservation Type");

        String[] options = {"Κανονική Δέσμευση", "Δέσμευση έκτακτης ανάγκης"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    NormalReservationFragment normalFragment = new NormalReservationFragment();
                    normalFragment.setSelectedDate(selectedDate);
                    normalFragment.setOnReservationSavedListener(this::onReservationSaved);
                    normalFragment.show(getSupportFragmentManager(), "NormalReservationFragment");
                    break;
                case 1:
                    EmergencyReservationFragment emergencyFragment = new EmergencyReservationFragment();
                    emergencyFragment.setSelectedDate(selectedDate);
                    emergencyFragment.setOnReservationSavedListener(this::onReservationSaved);
                    emergencyFragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
                    break;
            }
        });

        builder.create().show();
    }

    private void onReservationSaved() {
        fetchReservations();        // Refresh the reservations list
        fetchEventsForCalendar();   // Refresh the calendar dots
    }
}
