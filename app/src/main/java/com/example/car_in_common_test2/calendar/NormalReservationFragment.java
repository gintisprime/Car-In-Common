package com.example.car_in_common_test2.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.car_in_common_test2.R;

import java.util.Calendar;

public class NormalReservationFragment extends DialogFragment {

    private EditText startTimeEditText, endTimeEditText;
    private EditText dateEditText;
    private EditText reasonEditText;
    private Button saveReservationButton;

    private String selectedDate;

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_normal_reservation, container, false);

        startTimeEditText = view.findViewById(R.id.startTimeEditText);
        endTimeEditText = view.findViewById(R.id.endTimeEditText);
        reasonEditText = view.findViewById(R.id.reasonEditText);
        saveReservationButton = view.findViewById(R.id.saveNormalReservationButton);
        dateEditText = view.findViewById(R.id.dateEditText);

        // Set current date as default (if no date selected)
        if (selectedDate != null) {
            dateEditText.setText(selectedDate);
        }

        // DatePicker for Date selection
        dateEditText.setOnClickListener(v -> showDatePickerDialog());

        // TimePicker for Start Time
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog(startTimeEditText));

        // TimePicker for End Time
        endTimeEditText.setOnClickListener(v -> showTimePickerDialog(endTimeEditText));

        // Save the reservation
        saveReservationButton.setOnClickListener(v -> saveReservation());

        return view;
    }

    private void showTimePickerDialog(EditText timeEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (TimePicker view, int hourOfDay, int minuteOfHour) -> {
            String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
            timeEditText.setText(time);
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, yearSelected, monthOfYear, dayOfMonth) -> {
            // Format the selected date as dd-MM-yyyy
            String date = String.format("%02d-%02d-%04d", dayOfMonth, monthOfYear + 1, yearSelected);
            dateEditText.setText(date);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void saveReservation() {
        String reason = reasonEditText.getText().toString();
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();
        String date = dateEditText.getText().toString();

        if (reason.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || date.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Reservation newReservation = new Reservation(reason, startTime, endTime, date, false); // Normal reservation

        FirebaseHelper.checkForConflictsAndSave(date, startTime, endTime, newReservation, new FirebaseHelper.OnConflictCheckCallback() {
            @Override
            public void onConflict() {
                Toast.makeText(getContext(), "This time slot is already reserved. Please choose another.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Reservation saved successfully!", Toast.LENGTH_SHORT).show();
                dismiss(); // Close the dialog
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to save reservation.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
