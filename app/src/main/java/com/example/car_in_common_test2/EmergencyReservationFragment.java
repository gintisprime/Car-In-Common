package com.example.car_in_common_test2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmergencyReservationFragment extends BottomSheetDialogFragment {

    private NumberPicker durationPicker;
    private Button saveButton;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_reservation, container, false);

        durationPicker = view.findViewById(R.id.durationPicker);
        saveButton = view.findViewById(R.id.saveButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("reservations");

        // Configure the duration picker (e.g., 1 to 12 hours)
        durationPicker.setMinValue(1);
        durationPicker.setMaxValue(12);
        durationPicker.setValue(1);

        saveButton.setOnClickListener(v -> {
            int duration = durationPicker.getValue();
            String date = getArguments() != null ? getArguments().getString("selectedDate") : null;

            if (date == null) {
                Toast.makeText(getContext(), "No date selected. Please select a date.", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = databaseReference.push().getKey();
            Reservation reservation = new Reservation("Emergency", date, "Emergency reservation for " + duration + " hours");

            if (id != null) {
                databaseReference.child(id).setValue(reservation)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Emergency reservation added!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save reservation.", Toast.LENGTH_SHORT).show());
            }
        });

        return view;
    }
}
