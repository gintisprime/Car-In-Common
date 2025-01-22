package com.example.car_in_common_test2.vehicle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.calendar.Reservation;
import com.example.car_in_common_test2.utils.BaseActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MapsActivity";

    private DatabaseReference databaseReference;
    private DatabaseReference reservationsRef;

    private TextView lastReservationLabel;
    private TextView lastReservationDetails;

    private HashMap<Marker, String[]> markerDataMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout into BaseActivity's content frame
        getLayoutInflater().inflate(R.layout.activity_maps, findViewById(R.id.contentFrame), true);

        // Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        reservationsRef = FirebaseDatabase.getInstance().getReference("reservations");

        // Initialize views
        lastReservationLabel = findViewById(R.id.lastReservationLabel);
        lastReservationDetails = findViewById(R.id.lastReservationDetails);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            initializeMap();
        }

        // Fetch and display last reservation details
        fetchLastReservationDetails();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null; // Use default frame
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate custom InfoWindow layout
                View view = LayoutInflater.from(MapsActivity.this).inflate(R.layout.info_window, null);

                TextView carModelText = view.findViewById(R.id.carModel);
                TextView carDetailsText = view.findViewById(R.id.carDetails);

                // Retrieve data for this marker
                String[] details = markerDataMap.get(marker);
                if (details != null) {
                    carModelText.setText("Team: " + details[0]); // Team Name
                    carDetailsText.setText("Model: " + details[1] + " | Plate: " + details[2]); // Car Model and Plate
                }

                return view;
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fetchAndShowUserLocation();
            loadCarDetailsFromFirebase();
        }
    }

    private void fetchAndShowUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("You are here!"));

                        if (marker != null) {
                            marker.showInfoWindow();
                        }
                    } else {
                        Toast.makeText(this, "Unable to get location. Ensure location services are enabled.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    Toast.makeText(this, "Failed to get location. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCarDetailsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear(); // Clear previous markers
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    DataSnapshot carDetailsSnapshot = userSnapshot.child("carDetails");

                    String teamName = carDetailsSnapshot.child("teamName").getValue(String.class);
                    String carModel = carDetailsSnapshot.child("carModel").getValue(String.class);
                    String carPlate = carDetailsSnapshot.child("carPlate").getValue(String.class);
                    Double latitude = carDetailsSnapshot.child("latitude").getValue(Double.class);
                    Double longitude = carDetailsSnapshot.child("longitude").getValue(Double.class);

                    if (teamName != null && carModel != null && carPlate != null && latitude != null && longitude != null) {
                        LatLng latLng = new LatLng(latitude, longitude);
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(teamName));

                        if (marker != null) {
                            markerDataMap.put(marker, new String[]{teamName, carModel, carPlate});
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapsActivity.this, "Failed to load car details.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void fetchLastReservationDetails() {
        String today = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        reservationsRef.orderByChild("date").equalTo(today)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Reservation lastReservation = null;

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Reservation reservation = data.getValue(Reservation.class);
                            if (reservation != null) {
                                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                if (reservation.getEndTime().compareTo(currentTime) > 0) {
                                    if (lastReservation == null || reservation.getEndTime().compareTo(lastReservation.getEndTime()) < 0) {
                                        lastReservation = reservation;
                                    }
                                }
                            }
                        }

                        if (lastReservation != null) {
                            lastReservationLabel.setVisibility(View.VISIBLE);
                            lastReservationDetails.setVisibility(View.VISIBLE);
                            String details = "Ημερομηνία: " + lastReservation.getDate() + "\n" +
                                    "Ώρα: " + lastReservation.getStartTime() + " - " + lastReservation.getEndTime() + "\n" +
                                    "Τύπος: " + (lastReservation.isEmergency() ? "Επείγουσα" : "Κανονική");
                            lastReservationDetails.setText(details);
                        } else {
                            lastReservationLabel.setVisibility(View.GONE);
                            lastReservationDetails.setVisibility(View.VISIBLE);
                            lastReservationDetails.setText("Δεν υπάρχουν δέσμευσεις για σήμερα.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching reservations: " + error.getMessage());
                        Toast.makeText(MapsActivity.this, "Σφάλμα φόρτωσης δέσμευσης", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeMap();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMap == null) {
            initializeMap();
        }
    }
}
