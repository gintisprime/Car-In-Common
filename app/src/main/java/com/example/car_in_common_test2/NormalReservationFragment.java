package com.example.car_in_common_test2;

import android.app.DatePickerDialog;
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

import com.example.car_in_common_test2.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NormalReservationFragment extends DialogFragment {

    private EditText dateEditText, startTimeEditText, endTimeEditText, reasonEditText;
    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_normal_reservation, container, false);

        dateEditText = view.findViewById(R.id.dateEditText);
        startTimeEditText = view.findViewById(R.id.startTimeEditText);
        endTimeEditText = view.findViewById(R.id.endTimeEditText);
        reasonEditText = view.findViewById(R.id.reasonEditText);
        Button saveButton = view.findViewById(R.id.saveNormalReservationButton);

        // Set current date as default
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(calendar.getTime());
        dateEditText.setText(selectedDate);

        // Open DatePicker when clicking the date field
        dateEditText.setOnClickListener(v -> showDatePickerDialog(calendar));

        // Implement save button functionality
        saveButton.setOnClickListener(v -> saveReservation());

        return view;
    }

    private void showDatePickerDialog(Calendar calendar) {
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDate = sdf.format(calendar.getTime());
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveReservation() {
        String reason = reasonEditText.getText().toString();
        String startTime = startTimeEditText.getText().toString();
        String endTime = endTimeEditText.getText().toString();

        if (reason.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(getContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        Reservation newReservation = new Reservation(reason, startTime, endTime, selectedDate, false, false);
        FirebaseHelper.checkForConflictsAndSave(selectedDate, startTime, endTime, newReservation, new FirebaseHelper.OnConflictCheckCallback() {
            @Override
            public void onConflict() {
                Toast.makeText(getContext(), "Έχει καταχωρηθεί ήδη δέσμευση για αυτή την ώρα.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Η δέσμευση καταχωρήθηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Αποτυχία καταχώρησης δέσμευσης. Προσπάσθησε ξανά!", Toast.LENGTH_SHORT).show();
            }
        });

        dismiss();
    }

    public void setSelectedDate(String selectedDate) {
    }
}
