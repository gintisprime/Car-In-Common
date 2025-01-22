package com.example.car_in_common_test2.main_menu;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.auth.LoginActivity;
import com.example.car_in_common_test2.auth.StartScreenActivity;
import com.example.car_in_common_test2.utils.BaseActivity;
import com.example.car_in_common_test2.vehicle.CarDetailsActivity;
import com.example.car_in_common_test2.vehicle.ObdActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainMenuActivity extends BaseActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DrawerLayout drawerLayout;
    private TextView fuelTankLevel; // TextView for fuel level
    private ImageView fuelNeedle, fuelMeter; // ImageView for the needle and fuel meter
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable fuelLevelRunnable;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate activity_main_menu into contentFrame from BaseActivity
        getLayoutInflater().inflate(R.layout.activity_main_menu, findViewById(R.id.contentFrame), true);

        // Initialize Firebase and UI components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        drawerLayout = findViewById(R.id.drawerLayout);

        fuelTankLevel = findViewById(R.id.fuelLevelValue); // Fuel level text
        fuelNeedle = findViewById(R.id.fuel_needle); // Needle ImageView
        fuelMeter = findViewById(R.id.fuel_meter); // Fuel meter ImageView

        // Set initial pivot point dynamically and set to default position
        initializePivotAndSetDefaultPosition();

        // Sign-Out Button
        Button signOutButton = findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(v -> signOutUser());

        // Fetch user data
        fetchUserData();

        // Start fuel level updates
        startFuelLevelUpdates();
    }

    /**
     * Sets the needle pivot dynamically to the far right of the needle asset
     * and ensures it starts at the default position.
     */
    private void initializePivotAndSetDefaultPosition() {
        fuelNeedle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Set the pivot dynamically to the far-right of the needle
                fuelNeedle.setPivotX(fuelNeedle.getWidth() + 20f); // Far-right edge
                fuelNeedle.setPivotY(fuelNeedle.getHeight() / 2.0f); // Vertically centered

                // Scale the needle if required
                fuelNeedle.setScaleX(0.9f);
                fuelNeedle.setScaleY(0.8f);

                // Set the needle to the default upright position (90°)
                fuelNeedle.setRotation(90f); // Default rotation angle

                // Remove the listener to avoid redundant calls
                fuelNeedle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference usersRef = mDatabase.child("users");

            usersRef.child(currentUserId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("surname").getValue(String.class) + " " +
                            snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    ((TextView) findViewById(R.id.userFullName)).setText(fullName != null ? fullName : "Full Name Unavailable");
                    ((TextView) findViewById(R.id.userEmail)).setText(email != null ? email : "Email Unavailable");
                }
            }).addOnFailureListener(e -> {
                ((TextView) findViewById(R.id.userFullName)).setText("Error loading data");
                ((TextView) findViewById(R.id.userEmail)).setText("Error loading data");
            });
        }
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

    private void updateFuelMeterImage(double fuelLevelPercentage) {
        if (fuelLevelPercentage >= 85) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_7); // 85%-100%
        } else if (fuelLevelPercentage >= 70) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_6); // 70%-84%
        } else if (fuelLevelPercentage >= 55) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_5); // 55%-69%
        } else if (fuelLevelPercentage >= 40) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_4); // 40%-54%
        } else if (fuelLevelPercentage >= 25) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_3); // 25%-39%
        } else if (fuelLevelPercentage >= 10) {
            fuelMeter.setImageResource(R.drawable.fuel_meter_2); // 10%-24%
        } else {
            fuelMeter.setImageResource(R.drawable.fuel_meter_1); // 0%-9%
        }
    }

    private void fetchFuelLevel() {
        ObdActivity obdActivity = new ObdActivity();
        obdActivity.fetchFuelLevel(new ObdActivity.FuelLevelListener() {
            @Override
            public void onFuelLevelReceived(String fuelLevel) {
                runOnUiThread(() -> {
                    try {
                        // Update fuel tank text
                        fuelTankLevel.setText("Fuel Level: " + fuelLevel);

                        // Parse the fuel level percentage
                        double fuelLevelPercentage = Double.parseDouble(fuelLevel.replace("%", ""));
                        float rotationDegree = (float) (fuelLevelPercentage * 1.8); // Calculate rotation (0% = 0°, 100% = 180°)

                        // Update the fuel meter image
                        updateFuelMeterImage(fuelLevelPercentage);

                        // Animate needle to the calculated rotation
                        animateNeedle(rotationDegree);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainMenuActivity.this, "Invalid fuel level data", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() -> {
                    // Show error message
                    fuelTankLevel.setText(errorMessage);

                    // Set needle to default position (90°) when OBD is disconnected
                    animateNeedle(90f); // Default upright position
                });
            }
        });
    }

    /**
     * Rotates the needle ImageView around its far-right corner to the specified angle.
     *
     * @param angle The angle to rotate the needle (in degrees).
     */
    private void animateNeedle(float angle) {
        // Smooth rotation animation
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(fuelNeedle, "rotation", fuelNeedle.getRotation(), angle);
        rotationAnimator.setDuration(1000); // Animation duration
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotationAnimator.start();
    }

    private void signOutUser() {
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(MainMenuActivity.this, StartScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && fuelLevelRunnable != null) {
            handler.removeCallbacks(fuelLevelRunnable);
        }
    }
}
