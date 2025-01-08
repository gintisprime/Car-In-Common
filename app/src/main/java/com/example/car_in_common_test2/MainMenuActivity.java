package com.example.car_in_common_test2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenuActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DrawerLayout drawerLayout;
    private TextView fuelTankLevel, usernameDisplay, emailDisplay;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fuelLevelRunnable;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main_menu, findViewById(R.id.contentFrame));

        // Initialize Firebase and UI components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        drawerLayout = findViewById(R.id.drawerLayout);

        fuelTankLevel = findViewById(R.id.fuelLevelValue);

        // Initialize TextViews for full name and email
        TextView userFullName = findViewById(R.id.userFullName);
        TextView userEmail = findViewById(R.id.userEmail);

        Button signOutButton = findViewById(R.id.signOutButton);

        // Fetch user data from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

            usersRef.child(currentUserId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("surname").getValue(String.class) + " "
                            + snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    // Update TextViews
                    userFullName.setText(fullName != null ? fullName : "Full Name Unavailable");
                    userEmail.setText(email != null ? email : "Email Unavailable");
                }
            }).addOnFailureListener(e -> {
                // Handle errors gracefully
                userFullName.setText("Error loading data");
                userEmail.setText("Error loading data");
            });
        }

        // Add sign-out logic
        signOutButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Sign Out")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        mAuth.signOut();
                        Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, StartScreenActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Other initialization methods
        checkUserAuthentication();
        startFuelLevelUpdates();
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
        Toast.makeText(MainMenuActivity.this, "Συμπληρώστε τα στοιχεία του αυτοκινήτου.", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(() -> fuelTankLevel.setText("Ποσοστό Βενζίνης: " + fuelLevel));
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
