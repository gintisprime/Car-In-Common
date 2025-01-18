package com.example.car_in_common_test2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ReservationTypeDialogFragment extends DialogFragment {

    private String selectedDate;

    public void setSelectedDate(String date) {
        this.selectedDate = date;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reservation_type, container, false);

        Button normalButton = view.findViewById(R.id.normalReservationButton);
        Button emergencyButton = view.findViewById(R.id.emergencyReservationButton);

        // Open Normal Reservation Fragment
        normalButton.setOnClickListener(v -> {
            NormalReservationFragment normalFragment = new NormalReservationFragment();
            normalFragment.setSelectedDate(selectedDate);
            normalFragment.show(getParentFragmentManager(), "NormalReservationFragment");
            dismiss();
        });

        // Open Emergency Reservation Fragment
        emergencyButton.setOnClickListener(v -> {
            EmergencyReservationFragment emergencyFragment = new EmergencyReservationFragment();
            emergencyFragment.setSelectedDate(selectedDate);
            emergencyFragment.show(getParentFragmentManager(), "EmergencyReservationFragment");
            dismiss();
        });

        return view;
    }
}