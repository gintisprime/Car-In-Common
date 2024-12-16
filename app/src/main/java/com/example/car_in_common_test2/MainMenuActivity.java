    package com.example.car_in_common_test2;

    import android.annotation.SuppressLint;
    import android.content.Intent;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.view.GravityCompat;
    import androidx.drawerlayout.widget.DrawerLayout;

    import com.google.android.material.navigation.NavigationView;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.database.DataSnapshot;
    import com.google.firebase.database.DatabaseReference;
    import com.google.firebase.database.FirebaseDatabase;

    public class MainMenuActivity extends AppCompatActivity {

        private FirebaseAuth mAuth;
        private DatabaseReference mDatabase;
        private DrawerLayout drawerLayout;
        private TextView fuelTankLevel, usernameDisplay; // Add username display TextView
        private Handler handler = new Handler(Looper.getMainLooper());
        private Runnable fuelLevelRunnable;

        // Add references for navigation buttons and account icon
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
            usernameDisplay = findViewById(R.id.username); // Initialize usernameDisplay

            // Reference navigation buttons and account icon
            navHome = findViewById(R.id.navHome);
            navCalendar = findViewById(R.id.navCalendar);
            navMaps = findViewById(R.id.navMaps);
            navTransactions = findViewById(R.id.navTransactions);
            navChat = findViewById(R.id.navChat);
            accountIcon = findViewById(R.id.accountIcon);

            // Set button click listeners
            setButtonListeners();

            checkUserAuthentication();

            // Start fetching fuel level every 5 seconds
            startFuelLevelUpdates();
        }

        private void setButtonListeners() {
            navHome.setOnClickListener(v -> Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show());
            navCalendar.setOnClickListener(v -> Toast.makeText(this, "Calendar clicked", Toast.LENGTH_SHORT).show());
            navMaps.setOnClickListener(v -> {
                Intent intent = new Intent(MainMenuActivity.this, MapsActivity.class);
                startActivity(intent);
            });
            navTransactions.setOnClickListener(v -> Toast.makeText(this, "Transactions clicked", Toast.LENGTH_SHORT).show());
            navChat.setOnClickListener(v -> {
                Intent intent = new Intent(MainMenuActivity.this, GroupChatActivity.class);
                startActivity(intent);
            });

            accountIcon.setOnClickListener(v -> {
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                } else {
                    Toast.makeText(this, "Error: Drawer not initialized.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void checkUserAuthentication() {
            if (mAuth.getCurrentUser() != null) {
                String userId = mAuth.getCurrentUser().getUid();

                // Fetch and display car details
                mDatabase.child("users").child(userId).child("carDetails").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        String teamName = dataSnapshot.child("teamName").getValue(String.class);
                        String model = dataSnapshot.child("carModel").getValue(String.class);
                        String plate = dataSnapshot.child("carPlate").getValue(String.class);

                        ((TextView) findViewById(R.id.carTeamName)).setText("Μάρκα: " + teamName);
                        ((TextView) findViewById(R.id.carModel)).setText("Μοντέλο: " + model);
                        ((TextView) findViewById(R.id.carPlate)).setText("Αριθμός Πινακίδας: " + plate);
                    } else {
                        startActivity(new Intent(this, CarDetailsActivity.class));
                        finish();
                    }
                });

                // Fetch and display the username
                mDatabase.child("users").child(userId).child("name").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String username = task.getResult().getValue(String.class);
                        if (usernameDisplay != null) {
                            usernameDisplay.setText(username != null ? username : "No Username Found");
                        } else {
                            Toast.makeText(this, "Username TextView is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        usernameDisplay.setText("Failed to fetch username");
                    }
                });

            } else {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        }

        private void startFuelLevelUpdates() {
            fuelLevelRunnable = new Runnable() {
                @Override
                public void run() {
                    fetchFuelLevel();
                    // Schedule the next execution after 5 seconds
                    handler.postDelayed(this, 5000);
                }
            };

            // Start the runnable immediately
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

            // Stop the handler when the activity is destroyed to avoid memory leaks
            if (handler != null && fuelLevelRunnable != null) {
                handler.removeCallbacks(fuelLevelRunnable);
            }
        }
    }
