package com.example.car_in_common_test2.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
        View view = inflater.inflate(R.layout.fragment_emergency_reservation, container, false);

        Spinner durationSpinner = view.findViewById(R.id.emergencyDurationSpinner);
        Button saveButton = view.findViewById(R.id.emergencySaveButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.duration_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> {
            if (!FirebaseHelper.isAuthenticated()) {
                Toast.makeText(getContext(), "Παρακαλώ συνδεθείτε για να κάνετε δέσμευση.", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedDuration = durationSpinner.getSelectedItemPosition() + 1;

            String startTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, selectedDuration);
            String endTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

            Reservation emergencyReservation = new Reservation();
            emergencyReservation.setReason("Emergency Reservation");
            emergencyReservation.setStartTime(startTime);
            emergencyReservation.setEndTime(endTime);
            emergencyReservation.setDate(selectedDate);
            emergencyReservation.setEmergency(true);
            emergencyReservation.setReleaseTimeCertain(false); // Not applicable for emergency reservations


            FirebaseHelper.saveReservationToFirebase(emergencyReservation, new FirebaseHelper.FirebaseCallback() {
                @Override
                public void onSuccess(Object result) {
                    Toast.makeText(getContext(), "Η δέσμευση έκτακτης ανάγκης καταχωρήθηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
                    if (onReservationSavedListener != null) {
                        onReservationSavedListener.onSaved();
                    }
                    dismiss();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Αποτυχία καταχώρησης δέσμευσης έκτακτης ανάγκης . Παρακαλώ προσπαθήστε πάλι.", Toast.LENGTH_SHORT).show();
                }
            });
        });
        return view;
    }

    public interface OnReservationSavedListener {
        void onSaved();
    }
}
