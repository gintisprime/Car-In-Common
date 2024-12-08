package com.example.car_in_common_test2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the buttons and text views by their IDs
        Button loginButton = findViewById(R.id.buttonLogin);
        Button signUpButton = findViewById(R.id.buttonSignUp);
        TextView menuTitle = findViewById(R.id.menuTitle);
        TextView accountInfo = findViewById(R.id.accountInfo);
        LinearLayout carDetailsLayout = findViewById(R.id.carDetailsLayout);
        Button signOutButton = findViewById(R.id.buttonSignOut);
        Button messagesButton = findViewById(R.id.buttonMessages);  // New Messages button

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if the user is logged in
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();

            // Fetch car details from Firebase database
            mDatabase.child("users").child(userId).child("car").get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    String registration = dataSnapshot.child("registrationNumber").getValue(String.class);
                    String brand = dataSnapshot.child("brand").getValue(String.class);
                    String model = dataSnapshot.child("model").getValue(String.class);
                    String color = dataSnapshot.child("color").getValue(String.class);

                    // Update the UI with car details
                    ((TextView) findViewById(R.id.carRegistration)).setText("Αριθμός πινακίδας: " + registration);
                    ((TextView) findViewById(R.id.carBrand)).setText("Μάρκα: " + brand);
                    ((TextView) findViewById(R.id.carModel)).setText("Μοντέλο: " + model);
                    ((TextView) findViewById(R.id.carColor)).setText("Χρώμα: " + color);

                    // Hide login/sign-up buttons and show car details
                    loginButton.setVisibility(View.GONE);
                    signUpButton.setVisibility(View.GONE);
                    accountInfo.setVisibility(View.VISIBLE);
                    carDetailsLayout.setVisibility(View.VISIBLE);
                    menuTitle.setVisibility(View.GONE);  // Hide "Welcome!" message

                    // Show the Messages button
                    messagesButton.setVisibility(View.VISIBLE);

                } else {
                    // If no car details, show login and sign-up buttons
                    loginButton.setVisibility(View.VISIBLE);
                    signUpButton.setVisibility(View.VISIBLE);
                    accountInfo.setVisibility(View.GONE);
                    carDetailsLayout.setVisibility(View.GONE);
                    messagesButton.setVisibility(View.GONE);  // Hide Messages button

                    // Show the "Welcome!" message
                    menuTitle.setVisibility(View.VISIBLE);
                }
            });

        } else {
            // If the user is not logged in, show the login and sign-up buttons
            loginButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            accountInfo.setVisibility(View.GONE);
            carDetailsLayout.setVisibility(View.GONE);
            messagesButton.setVisibility(View.GONE);  // Hide Messages button

            // Show the "Welcome!" message
            menuTitle.setVisibility(View.VISIBLE);
        }

        // Set onClick listeners for the login and sign-up buttons
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainMenuActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Handle Sign Out button click
        signOutButton.setOnClickListener(v -> {
            // Show a confirmation dialog
            new AlertDialog.Builder(MainMenuActivity.this)
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Sign out from Firebase and clear SharedPreferences
                        mAuth.signOut();
                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.apply();

                        // Show a toast confirming the sign out
                        Toast.makeText(MainMenuActivity.this, "You have signed out", Toast.LENGTH_SHORT).show();

                        // Go back to the main menu with login and sign-up buttons
                        Intent intent = new Intent(MainMenuActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    // Inflate the menu to add the Settings button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);  // Inflate main_menu.xml for the Settings option
        return true;
    }

    // Handle click events for the Settings button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Handle Settings button click here
            Intent intent = new Intent(MainMenuActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
