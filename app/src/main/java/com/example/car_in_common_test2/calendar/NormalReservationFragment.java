package com.example.car_in_common_test2.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.car_in_common_test2.R;

import java.util.Calendar;
import java.util.Locale;

public class NormalReservationFragment extends DialogFragment {

    private String selectedDate;
    private OnReservationSavedListener onReservationSavedListener;

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void setOnReservationSavedListener(OnReservationSavedListener listener) {
        this.onReservationSavedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_normal_reservation, container, false);

        EditText reasonEditText = view.findViewById(R.id.reasonEditText);
        EditText dateEditText = view.findViewById(R.id.dateEditText);
        EditText startTimeEditText = view.findViewById(R.id.startTimeEditText);
        EditText endTimeEditText = view.findViewById(R.id.endTimeEditText);
        RadioGroup releaseCertaintyRadioGroup = view.findViewById(R.id.releaseCertaintyRadioGroup);
        Button saveButton = view.findViewById(R.id.saveNormalReservationButton);

        // Set the selected date in the date field
        dateEditText.setText(selectedDate);

        // Date picker
        dateEditText.setOnClickListener(v -> showDatePickerDialog(dateEditText));

        // Time pickers for start and end time
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog(startTimeEditText));
        endTimeEditText.setOnClickListener(v -> showTimePickerDialog(endTimeEditText));

        saveButton.setOnClickListener(v -> {
            if (!FirebaseHelper.isAuthenticated()) {
                Toast.makeText(getContext(), "Please log in to save reservations.", Toast.LENGTH_SHORT).show();
                return;
            }

            String reason = reasonEditText.getText().toString();
            String date = dateEditText.getText().toString();
            String startTime = startTimeEditText.getText().toString();
            String endTime = endTimeEditText.getText().toString();
            boolean releaseTimeCertain = releaseCertaintyRadioGroup.getCheckedRadioButtonId() == R.id.radioYes;

            if (reason.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
                Toast.makeText(getContext(), "Συμπληρώστε όλα τα πεδία.", Toast.LENGTH_SHORT).show();
                return;
            }

            Reservation normalReservation = new Reservation();
            normalReservation.setReason(reason);
            normalReservation.setStartTime(startTime);
            normalReservation.setEndTime(endTime);
            normalReservation.setDate(date);
            normalReservation.setEmergency(false);
            normalReservation.setReleaseTimeCertain(releaseTimeCertain);


            FirebaseHelper.saveReservationToFirebase(normalReservation, new FirebaseHelper.FirebaseCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getContext(), "Επιτυχής καταχώρηση δέσμεσυης!", Toast.LENGTH_SHORT).show();
                    if (onReservationSavedListener != null) {
                        onReservationSavedListener.onSaved();
                    }
                    dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Αποτυχία καταχώρησης δέσμευσης. Παρακαλώ προσπαθήστε ξανά.", Toast.LENGTH_SHORT).show();
                }
            });
        });


        return view;
    }

    private void showDatePickerDialog(EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);
            dateEditText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePickerDialog(EditText timeEditText) {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            timeEditText.setText(selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }

    public interface OnReservationSavedListener {
        void onSaved();
    }
}
