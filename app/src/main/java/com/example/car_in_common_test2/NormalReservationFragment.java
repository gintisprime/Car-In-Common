package com.example.car_in_common_test2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NormalReservationFragment extends DialogFragment {

    private EditText reasonEditText, dateEditText, startTimeEditText, endTimeEditText;
    private RadioGroup importanceRadioGroup, releaseCertaintyRadioGroup;
    private Button confirmButton;

    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_normal_reservation, container, false);

        reasonEditText = view.findViewById(R.id.reasonEditText);
        dateEditText = view.findViewById(R.id.dateEditText);
        startTimeEditText = view.findViewById(R.id.startTimeEditText);
        endTimeEditText = view.findViewById(R.id.endTimeEditText);
        importanceRadioGroup = view.findViewById(R.id.importancyRadioGroup); // RadioGroup for importance
        releaseCertaintyRadioGroup = view.findViewById(R.id.releaseCertaintyRadioGroup);
        confirmButton = view.findViewById(R.id.saveNormalReservationButton);

        // Date picker for date selection
        dateEditText.setFocusable(false);
        dateEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                dateEditText.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Time picker for start time
        startTimeEditText.setFocusable(false);
        startTimeEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view12, hourOfDay, minute1) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute1);
                startTimeEditText.setText(time);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        // Time picker for end time
        endTimeEditText.setFocusable(false);
        endTimeEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view13, hourOfDay, minute1) -> {
                String time = String.format("%02d:%02d", hourOfDay, minute1);
                endTimeEditText.setText(time);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        // Confirm button
        confirmButton.setOnClickListener(v -> createReservation());
        return view;
    }

// Inside NormalReservationFragment.java

    private void createReservation() {
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(getContext(), "Please select a valid date.", Toast.LENGTH_SHORT).show();
            return; // Stop the method execution if the date is invalid
        }
        // Check if this method is being called
        Toast.makeText(getContext(), "Creating reservation...", Toast.LENGTH_SHORT).show();

        String reason = reasonEditText.getText().toString().trim();
        String startTime = startTimeEditText.getText().toString().trim();
        String endTime = endTimeEditText.getText().toString().trim();

        // Get the selected importance from the radio group
        int selectedImportanceId = importanceRadioGroup.getCheckedRadioButtonId();
        String importance = selectedImportanceId == R.id.important ? "High" : "Low";

        String releaseCertainty = ((RadioButton) getView().findViewById(releaseCertaintyRadioGroup.getCheckedRadioButtonId())).getText().toString();

        // Validation checks
        if (reason.isEmpty() || selectedDate == null || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if end time is after start time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);

            if (end.before(start)) {
                Toast.makeText(getContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Please enter valid times.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Saving data to Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reservations");
        String id = databaseReference.push().getKey();

// Log Firebase Reference
        Log.d("FirebaseDebug", "Database Reference: " + databaseReference.getKey());
        Log.d("FirebaseDebug", "Reservation ID: " + id);  // Check the ID generated by Firebase

        Reservation reservation = new Reservation(id, "Normal", selectedDate, reason, startTime, endTime, importance, releaseCertainty);

        databaseReference.child(selectedDate).child(id).setValue(reservation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Reservation created successfully!", Toast.LENGTH_SHORT).show();

                        // Notify CalendarActivity to refresh the calendar
                        CalendarActivity calendarActivity = (CalendarActivity) getActivity();
                        if (calendarActivity != null) {
                            calendarActivity.refreshReservations(selectedDate);
                        }
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), "Failed to create reservation.", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
