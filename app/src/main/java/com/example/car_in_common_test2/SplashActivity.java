package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // For the delay
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the splash screen layout (if you have a splash screen design, uncomment the next line)
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Add a delay to display the splash screen
        new Handler().postDelayed(() -> {
            // Check if the user is logged in
            if (mAuth.getCurrentUser() != null) {
                // User is logged in, navigate to MainMenuActivity
                Intent intent = new Intent(SplashActivity.this, MainMenuActivity.class);
                startActivity(intent);
            } else {
                // User is not logged in, navigate to StartScreenActivity
                Intent intent = new Intent(SplashActivity.this, StartScreenActivity.class);
                startActivity(intent);
            }

            // Close SplashActivity
            finish();
        }, 1000); // 2-second delay (adjust if needed)
    }
}
