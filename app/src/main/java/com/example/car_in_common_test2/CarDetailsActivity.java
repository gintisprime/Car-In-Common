package com.example.car_in_common_test2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CarDetailsActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 1001;
    private EditText teamNameEditText, carModelEditText, carPlateEditText;
    private ImageView profileImageView;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    // Activity result launcher for picking an image
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    profileImageView.setImageURI(imageUri);  // Set the selected image to the ImageView
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        profileImageView = findViewById(R.id.imageViewProfilePicture);
        teamNameEditText = findViewById(R.id.editTextTeamName);
        carModelEditText = findViewById(R.id.editTextCarModel);
        carPlateEditText = findViewById(R.id.editTextCarPlate);
        Button submitButton = findViewById(R.id.buttonSubmitCarDetails);

        // Handle profile picture upload
        Button uploadPictureButton = findViewById(R.id.buttonUploadPicture);
        uploadPictureButton.setOnClickListener(v -> {
            if (isStoragePermissionGranted()) {
                openImagePicker();
            } else {
                requestStoragePermission();
            }
        });

        // Handle form submission
        submitButton.setOnClickListener(v -> {
            String teamName = teamNameEditText.getText().toString().trim();
            String carModel = carModelEditText.getText().toString().trim();
            String carPlate = carPlateEditText.getText().toString().trim();

            if (TextUtils.isEmpty(teamName) || TextUtils.isEmpty(carModel) || TextUtils.isEmpty(carPlate)) {
                Toast.makeText(CarDetailsActivity.this, "Please enter all details.", Toast.LENGTH_SHORT).show();
            } else {
                saveCarDetailsToDatabase(teamName, carModel, carPlate);
            }
        });
    }

    // Check if storage permission is granted
    private boolean isStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permission
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Storage permission is needed to upload a profile picture.", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied. Cannot upload image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open the image picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void saveCarDetailsToDatabase(String teamName, String carModel, String carPlate) {
        String userId = mAuth.getCurrentUser().getUid();  // Get the current user's UID

        // Create a new car object
        Car car = new Car(teamName, carModel, carPlate);

        // Save car details under the user's record in the database
        mDatabase.child("users").child(userId).child("carDetails").setValue(car)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CarDetailsActivity.this, "Car details saved successfully!", Toast.LENGTH_SHORT).show();

                        // Save car details in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("team_name", teamName);
                        editor.putString("car_model", carModel);
                        editor.putString("car_plate", carPlate);
                        editor.apply();

                        // Redirect to MainMenuActivity
                        Intent intent = new Intent(CarDetailsActivity.this, MainMenuActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CarDetailsActivity.this, "Failed to save car details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Car class
    static class Car {
        public String teamName;
        public String carModel;
        public String carPlate;

        public Car(String teamName, String carModel, String carPlate) {
            this.teamName = teamName;
            this.carModel = carModel;
            this.carPlate = carPlate;
        }
    }
}
