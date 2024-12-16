package com.example.car_in_common_test2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenuActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DrawerLayout drawerLayout;
    private TextView fuelTankLevel, usernameDisplay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fuelLevelRunnable;

    private ImageView navHome, navCalendar, navMaps, navTransactions, navChat, accountIcon;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Initialize Firebase and UI components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        drawerLayout = findViewById(R.id.drawerLayout);

        fuelTankLevel = findViewById(R.id.fuelLevelValue);


        navHome = findViewById(R.id.navHome);
        navCalendar = findViewById(R.id.navCalendar);
        navMaps = findViewById(R.id.navMaps);
        navTransactions = findViewById(R.id.navTransactions);
        navChat = findViewById(R.id.navChat);
        accountIcon = findViewById(R.id.accountIcon);

        setButtonListeners();
        checkUserAuthentication();

        startFuelLevelUpdates();
    }

    private void setButtonListeners() {
        navHome.setOnClickListener(v -> Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show());
        navCalendar.setOnClickListener(v -> Toast.makeText(this, "Calendar clicked", Toast.LENGTH_SHORT).show());
        navMaps.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, MapsActivity.class)));
        navTransactions.setOnClickListener(v -> Toast.makeText(this, "Transactions clicked", Toast.LENGTH_SHORT).show());
        navChat.setOnClickListener(v -> startActivity(new Intent(MainMenuActivity.this, GroupChatActivity.class)));

        accountIcon.setOnClickListener(v -> {
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            } else {
                Toast.makeText(this, "Error: Drawer not initialized.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserAuthentication() {
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        // Step 1: Check if the user has a selectedCarId
        mDatabase.child("users").child(userId).child("selectedCarId").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String carId = task.getResult().getValue(String.class);
                if (carId != null) {
                    fetchCarDetails(carId);
                } else {
                    redirectToCarDetails();
                }
            } else {
                redirectToCarDetails();
            }
        });
    }

    private void fetchCarDetails(String carId) {
        // Step 2: Fetch car details using the selectedCarId
        mDatabase.child("cars").child(carId).get().addOnCompleteListener(carTask -> {
            if (carTask.isSuccessful() && carTask.getResult().exists()) {
                DataSnapshot carSnapshot = carTask.getResult();
                String teamName = carSnapshot.child("teamName").getValue(String.class);
                String model = carSnapshot.child("carModel").getValue(String.class);
                String plate = carSnapshot.child("carPlate").getValue(String.class);

                // Update UI with car details
                ((TextView) findViewById(R.id.carTeamName)).setText("Μάρκα: " + teamName);
                ((TextView) findViewById(R.id.carModel)).setText("Μοντέλο: " + model);
                ((TextView) findViewById(R.id.carPlate)).setText("Αριθμός Πινακίδας: " + plate);
            } else {
                Toast.makeText(this, "Failed to load car details.", Toast.LENGTH_SHORT).show();
                redirectToCarDetails();
            }
        });
    }

    private void redirectToCarDetails() {
        Toast.makeText(MainMenuActivity.this, "Please provide car details.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, CarDetailsActivity.class));
        finish();
    }

    private void startFuelLevelUpdates() {
        fuelLevelRunnable = new Runnable() {
            @Override
            public void run() {
                fetchFuelLevel();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(fuelLevelRunnable);
    }

    private void fetchFuelLevel() {
        ObdActivity obdActivity = new ObdActivity();
        obdActivity.fetchFuelLevel(new ObdActivity.FuelLevelListener() {
            @Override
            public void onFuelLevelReceived(String fuelLevel) {
                runOnUiThread(() -> fuelTankLevel.setText("Fuel Level: " + fuelLevel));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> fuelTankLevel.setText(errorMessage));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && fuelLevelRunnable != null) {
            handler.removeCallbacks(fuelLevelRunnable);
        }
    }
}
