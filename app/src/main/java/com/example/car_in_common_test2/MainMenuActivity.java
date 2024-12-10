package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Get UI components by their IDs
        TextView pageTitle = findViewById(R.id.pageTitle);
        ImageView navMaps = findViewById(R.id.navMaps);
        ProgressBar fuelIndicator = findViewById(R.id.fuelIndicator);
        ImageView accountIcon = findViewById(R.id.accountIcon);
        LinearLayout navigationButtons = findViewById(R.id.navigationButtons);
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navCalendar = findViewById(R.id.navCalendar);
        ImageView navTransactions = findViewById(R.id.navTransactions);
        ImageView navChat = findViewById(R.id.navChat);

        // Car details section
        TextView carTeamName = findViewById(R.id.carTeamName);
        TextView carModel = findViewById(R.id.carModel);
        TextView carPlate = findViewById(R.id.carPlate);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if the user is logged in
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Fetch car details from Firebase
            mDatabase.child("users").child(userId).child("carDetails").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    String teamName = dataSnapshot.child("teamName").getValue(String.class);
                    String model = dataSnapshot.child("carModel").getValue(String.class);
                    String plate = dataSnapshot.child("carPlate").getValue(String.class);

                    // Populate the car details in the UI
                    carTeamName.setText("Μάρκα: " + teamName);
                    carModel.setText("Μοντέλο: " + model);
                    carPlate.setText("Αριθμός Πινακίδας: " + plate);

                } else {
                    // Redirect to CarDetailsActivity if car details are missing
                    Intent intent = new Intent(MainMenuActivity.this, CarDetailsActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

        } else {
            // Redirect to LoginActivity if user is not logged in
            Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Navigation button listeners
        navHome.setOnClickListener(v -> Toast.makeText(MainMenuActivity.this, "Home clicked", Toast.LENGTH_SHORT).show());
        navCalendar.setOnClickListener(v -> Toast.makeText(MainMenuActivity.this, "Calendar clicked", Toast.LENGTH_SHORT).show());
        navTransactions.setOnClickListener(v -> Toast.makeText(MainMenuActivity.this, "Transactions clicked", Toast.LENGTH_SHORT).show());
        navChat.setOnClickListener(v -> Toast.makeText(MainMenuActivity.this, "Chat clicked", Toast.LENGTH_SHORT).show());

        // Maps navigation listener
        navMaps.setOnClickListener(v -> {
            // Launch the MapsActivity
            Intent intent = new Intent(MainMenuActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        // Account icon click listener
        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        //fetchFuelLevel();

    }



//    private void fetchFuelLevel() {
//        // Example of connecting to OBD API (pseudo-code)
//        OBDInterface obd = new OBDInterface();
//        obd.connect();
//
//        obd.getFuelLevel(new OBDInterface.FuelLevelCallback() {
//            @Override
//            public void onFuelLevelFetched(int fuelLevel) {
//                runOnUiThread(() -> {
//                    // Update ProgressBar with fetched fuel level
//                    fuelIndicator.setProgress(fuelLevel);
//                    Toast.makeText(MainMenuActivity.this, "Fuel Level: " + fuelLevel + "%", Toast.LENGTH_SHORT).show();
//                });
//            }
//
//            @Override
//            public void onError(String error) {
//                runOnUiThread(() -> {
//                    Toast.makeText(MainMenuActivity.this, "Failed to fetch fuel level: " + error, Toast.LENGTH_SHORT).show();
//                });
//            }
//        });
//    }

    // Inflate the menu to add the Settings button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }




    // Handle click events for the Settings button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Open the SettingsActivity
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
