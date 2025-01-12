package com.example.car_in_common_test2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class NormalReservationFragment extends DialogFragment {

    private EditText reasonEditText, dateEditText, startTimeEditText, endTimeEditText;
    private Spinner importanceSpinner;
    private RadioGroup releaseTimeRadioGroup;
    private Button saveNormalReservationButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_normal_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        reasonEditText = view.findViewById(R.id.reasonEditText);
        dateEditText = view.findViewById(R.id.dateEditText);
        startTimeEditText = view.findViewById(R.id.startTimeEditText);
        endTimeEditText = view.findViewById(R.id.endTimeEditText);
        releaseTimeRadioGroup = view.findViewById(R.id.releaseTimeRadioGroup);
        saveNormalReservationButton = view.findViewById(R.id.saveNormalReservationButton);

        // Set date picker for dateEditText
        dateEditText.setOnClickListener(v -> showDatePicker());

        // Set time picker for startTimeEditText
        startTimeEditText.setOnClickListener(v -> showTimePicker(startTimeEditText));

        // Set time picker for endTimeEditText
        endTimeEditText.setOnClickListener(v -> showTimePicker(endTimeEditText));

        // Save button logic
        saveNormalReservationButton.setOnClickListener(v -> saveReservation());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String date = year + "-" + (month + 1) + "-" + dayOfMonth;
            dateEditText.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(EditText timeEditText) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String time = String.format("%02d:%02d", hourOfDay, minute);
            timeEditText.setText(time);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    private void saveReservation() {
        String reason = reasonEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();
        String importance = importanceSpinner.getSelectedItem().toString();

        int selectedRadioId = releaseTimeRadioGroup.getCheckedRadioButtonId();
        String releaseTimeCertain = null;

        if (selectedRadioId == R.id.radioYes) {
            releaseTimeCertain = "Yes";
        } else if (selectedRadioId == R.id.radioNo) {
            releaseTimeCertain = "No";
        }

        if (reason.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || releaseTimeCertain == null) {
            Toast.makeText(getContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save reservation to Firebase (or other backend)
        Toast.makeText(getContext(), "Reservation saved!", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
