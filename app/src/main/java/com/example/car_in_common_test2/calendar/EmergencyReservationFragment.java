package com.example.car_in_common_test2.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.calendar.FirebaseHelper;
import com.example.car_in_common_test2.calendar.Reservation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EmergencyReservationFragment extends DialogFragment {

    private String selectedDate;

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_reservation, container, false);

        Spinner durationSpinner = view.findViewById(R.id.emergencyDurationSpinner);
        Button saveButton = view.findViewById(R.id.emergencySaveButton);

        // Create an ArrayAdapter for the Spinner with durations (e.g., 1–5 hours)
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.duration_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            int selectedDuration = durationSpinner.getSelectedItemPosition() + 1; // Duration in hours

            if (!isCurrentDateValid()) {
                Toast.makeText(getContext(), "Emergency reservations can only be made for the current date.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveEmergencyReservation(selectedDuration);
            dismiss(); // Close dialog
        });

        return view;
    }

    private boolean isCurrentDateValid() {
        // Get the current date in the same format as selectedDate
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Ensure the selected date matches the current date
        return selectedDate.equals(currentDate);
    }

    private void saveEmergencyReservation(int duration) {
        // Get the current time as the start time
        String startTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Calculate the end time based on the selected duration
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, duration);
        String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

        // Create a new reservation object
        Reservation newReservation = new Reservation("Emergency Reservation", startTime, endTime, selectedDate, true);

        // Save the reservation to Firebase
        FirebaseHelper.checkForConflictsAndSave(selectedDate, startTime, endTime, newReservation, new FirebaseHelper.OnConflictCheckCallback() {
            @Override
            public void onConflict() {
                Toast.makeText(getContext(), "This time slot is already reserved. Please choose another.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Emergency reservation saved successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to save reservation.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
