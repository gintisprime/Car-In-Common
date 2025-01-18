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

public class EmergencyReservationFragment extends DialogFragment {

    private String selectedDate;

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_reservation, container, false);

        Spinner durationSpinner = view.findViewById(R.id.emergencyDurationSpinner);  // Spinner for selecting duration
        Button saveButton = view.findViewById(R.id.emergencySaveButton);

        // Create an ArrayAdapter for the Spinner with values 1 to 5
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.duration_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            int selectedDuration = durationSpinner.getSelectedItemPosition() + 1;  // Get the selected duration (1-5)

            // Ensure that the user picks a valid duration
            if (selectedDuration < 1 || selectedDuration > 5) {
                Toast.makeText(getContext(), "Please choose a valid duration.", Toast.LENGTH_SHORT).show();
            } else {
                // Handle the logic for saving the emergency reservation
                saveEmergencyReservation(selectedDuration);  // Add logic to handle the reservation with the chosen duration
                dismiss(); // Close dialog
            }
        });

        return view;
    }

    private void saveEmergencyReservation(int duration) {
        // Handle the logic for saving the emergency reservation (e.g., updating Firebase, updating calendar)
        // You can use the selectedDate and duration to create the reservation data.
        Toast.makeText(getContext(), "Emergency reservation for " + duration + " hours saved.", Toast.LENGTH_SHORT).show();
        // You would typically save this data to Firebase or local storage
    }
}