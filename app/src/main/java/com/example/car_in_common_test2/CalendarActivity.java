package com.example.car_in_common_test2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private CalendarView calendarView;
    private Button saveReservationButton, selectDateButton, selectTimeButton;
    private TextView reservationDetailsTextView;
    private ArrayList<Reservation> reservationsList;

    private String selectedDate = "";
    private String startTime = "";
    private String endTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        try {
            // Initialize Firebase
            database = FirebaseDatabase.getInstance();
            myRef = database.getReference("reservations");

            // Initialize views
            calendarView = findViewById(R.id.calendarView);
            saveReservationButton = findViewById(R.id.saveReservationButton);
            selectDateButton = findViewById(R.id.selectDateButton);
            selectTimeButton = findViewById(R.id.selectTimeButton);
            reservationDetailsTextView = findViewById(R.id.reservationDetailsTextView);

            reservationsList = new ArrayList<>();

            // Load all reservations from Firebase
            loadReservations();

            // Calendar date click listener
            calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                displayReservationsForDay(selectedDate);
            });

            // Save reservation button logic
            saveReservationButton.setOnClickListener(v -> saveReservation());

            // Select date button logic
            selectDateButton.setOnClickListener(v -> showDatePickerDialog());

            // Select time button logic
            selectTimeButton.setOnClickListener(v -> showTimePickerDialog());

        } catch (Exception e) {
            Log.e("CalendarActivity", "Error during initialization", e);
            Toast.makeText(this, "Error occurred during initialization", Toast.LENGTH_SHORT).show();
        }
    }

    // Reservation model class
    public class Reservation {
        private String date;
        private String startTime;
        private String endTime;

        public Reservation() {
            // Default constructor required for calls to DataSnapshot.getValue(Reservation.class)
        }

        public Reservation(String date, String startTime, String endTime) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getDate() {
            return date;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }
    }

    // Load all reservations from Firebase
    private void loadReservations() {
        try {
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    reservationsList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Reservation reservation = snapshot.getValue(Reservation.class);
                        reservationsList.add(reservation);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("Firebase", "Error loading reservations", error.toException());
                }
            });
        } catch (Exception e) {
            Log.e("Firebase", "Error loading reservations", e);
        }
    }

    // Display reservations for a specific day
    private void displayReservationsForDay(String date) {
        StringBuilder details = new StringBuilder("Reservations for " + date + ":\n");
        for (Reservation reservation : reservationsList) {
            if (reservation.getDate().equals(date)) {
                details.append(reservation.getStartTime())
                        .append(" - ")
                        .append(reservation.getEndTime())
                        .append("\n");
            }
        }
        reservationDetailsTextView.setText(details.toString());
    }

    // Save a reservation to Firebase
    private void saveReservation() {
        try {
            if (selectedDate.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(this, "Please select date and time.", Toast.LENGTH_SHORT).show();
                return;
            }

            Reservation reservation = new Reservation(selectedDate, startTime, endTime);
            myRef.push().setValue(reservation);
            Toast.makeText(this, "Reservation saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("SaveReservation", "Error saving reservation", e);
            Toast.makeText(this, "Error saving reservation", Toast.LENGTH_SHORT).show();
        }
    }

    // Show Date Picker Dialog
    private void showDatePickerDialog() {
        try {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth;
                selectDateButton.setText("Date: " + selectedDate); // Show selected date on button
            }, year, month, dayOfMonth);

            datePickerDialog.show();
        } catch (Exception e) {
            Log.e("DatePicker", "Error showing date picker", e);
        }
    }

    // Show Time Picker Dialog
    private void showTimePickerDialog() {
        try {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
                // Set start time
                if (startTime.isEmpty()) {
                    startTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    selectTimeButton.setText("Start Time: " + startTime); // Show selected start time
                } else {
                    // Set end time
                    endTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    selectTimeButton.setText("End Time: " + endTime); // Show selected end time
                }
            }, hour, minute, true);

            timePickerDialog.show();
        } catch (Exception e) {
            Log.e("TimePicker", "Error showing time picker", e);
        }
    }
}
