package com.example.car_in_common_test2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EmergencyReservationFragment extends DialogFragment {

    private EditText durationPicker;
    private Button saveButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_reservation, container, false);

        durationPicker = view.findViewById(R.id.durationPicker);
        saveButton = view.findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> createEmergencyReservation());
        return view;
    }

    private void createEmergencyReservation() {
        String durationStr = durationPicker.getText().toString().trim();

        if (durationStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a duration", Toast.LENGTH_SHORT).show();
            return;
        }

        int duration = Integer.parseInt(durationStr);
        if (duration < 1 || duration > 5) {
            Toast.makeText(getContext(), "Duration must be between 1 and 5 hours", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedDate = getArguments().getString("selectedDate");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reservations");

        String id = databaseReference.push().getKey();
        Reservation reservation = new Reservation(id, "Emergency", selectedDate, "Emergency Reservation", "Now", "In " + duration + " hours", "High", "Yes");

        // Save emergency reservation to Firebase
        databaseReference.child(selectedDate).child(id).setValue(reservation).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Emergency reservation created successfully!", Toast.LENGTH_SHORT).show();
                dismiss();
            } else {
                Toast.makeText(getContext(), "Failed to create emergency reservation.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
