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
        fetchReservations();

        // Handle date selection
        calendarView.setOnDayClickListener(eventDay -> {
            Date clickedDate = eventDay.getCalendar().getTime();
            selectedDate = sdf.format(clickedDate);
            selectedDateTextView.setText("Reservations for: " + selectedDate);
            fetchReservations();
        });

        // Add a new reservation
        addReservationButton.setOnClickListener(v -> showReservationOptions());
    }

    private void fetchReservations() {
        FirebaseHelper.fetchReservationsForDate(selectedDate, new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                reservationList.clear();
                reservationList.addAll(reservations);
                reservationAdapter.notifyDataSetChanged();
                Log.d("CalendarActivity", "Fetched reservations: " + reservations);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CalendarActivity", "Error fetching reservations", e);
                Toast.makeText(CalendarActivity.this, "Error fetching reservations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showReservationOptions() {
        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Reservation Type");

        // Options: Normal or Emergency
        String[] options = {"Normal Reservation", "Emergency Reservation"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Normal Reservation
                    NormalReservationFragment normalFragment = new NormalReservationFragment();
                    normalFragment.setSelectedDate(selectedDate);
                    normalFragment.show(getSupportFragmentManager(), "NormalReservationFragment");
                    break;

                case 1: // Emergency Reservation
                    EmergencyReservationFragment emergencyFragment = new EmergencyReservationFragment();
                    emergencyFragment.setSelectedDate(selectedDate);
                    emergencyFragment.show(getSupportFragmentManager(), "EmergencyReservationFragment");
                    break;
            }
        });

        // Display the dialog
        builder.create().show();
    }
}
