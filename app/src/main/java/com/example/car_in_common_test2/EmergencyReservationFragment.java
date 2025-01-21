package com.example.car_in_common_test2;

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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.duration_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            int selectedDuration = durationSpinner.getSelectedItemPosition() + 1;

            if (!isCurrentDateValid()) {
                Toast.makeText(getContext(), "Η δέσμευση έκτακτης ανάγκης γίνεται μόνο την τρέχουσα ημέρα.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveEmergencyReservation(selectedDuration);
            dismiss();
        });

        return view;
    }

    private boolean isCurrentDateValid() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        return selectedDate.equals(currentDate);
    }

    private void saveEmergencyReservation(int duration) {
        String startTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, duration);
        String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

        Reservation newReservation = new Reservation("Emergency", startTime, endTime, selectedDate, false, true);

        FirebaseHelper.checkForConflictsAndSave(selectedDate, startTime, endTime, newReservation, new FirebaseHelper.OnConflictCheckCallback() {
            @Override
            public void onConflict() {
                Toast.makeText(getContext(), "Conflict: Reservation exists for this time.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Emergency reservation saved successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Failed to save emergency reservation. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
