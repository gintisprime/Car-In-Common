package com.example.car_in_common_test2.utils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.car_in_common_test2.main_menu.MainMenuActivity;
import com.example.car_in_common_test2.vehicle.MapsActivity;
import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.transactions.TransactionsActivity;
import com.example.car_in_common_test2.calendar.CalendarActivity;
import com.example.car_in_common_test2.chat.ChatActivity;
import com.example.car_in_common_test2.chat.GroupChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Initialize navigation bar buttons
        ImageView navHome = findViewById(R.id.navHome);
        ImageView navCalendar = findViewById(R.id.navCalendar);
        ImageView navMaps = findViewById(R.id.navMaps);
        ImageView navTransactions = findViewById(R.id.navTransactions);
        ImageView navChat = findViewById(R.id.navChat);

        // Debug: Ensure buttons are initialized
        Log.d(TAG, "navHome initialized: " + (navHome != null));
        Log.d(TAG, "navCalendar initialized: " + (navCalendar != null));

        // Set navigation listeners
        navHome.setOnClickListener(v -> {
            Log.d(TAG, "Home clicked");
            navigateTo(MainMenuActivity.class);
        });

        navCalendar.setOnClickListener(v -> {
            Log.d(TAG, "Calendar clicked");
            navigateTo(CalendarActivity.class);
        });

        navMaps.setOnClickListener(v -> {
            Log.d(TAG, "Maps clicked");
            navigateTo(MapsActivity.class);
        });

        navTransactions.setOnClickListener(v -> {
            Log.d(TAG, "Transactions clicked");
            navigateTo(TransactionsActivity.class);
        });

        navChat.setOnClickListener(v -> {
            Log.d(TAG, "Chat clicked");
            navigateToChat();
        });
    }

    private void navigateTo(Class<?> targetActivity) {
        try {
            if (!this.getClass().equals(targetActivity)) { // Avoid reloading the current activity
                Log.d(TAG, "Navigating to: " + targetActivity.getSimpleName());
                startActivity(new Intent(this, targetActivity));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to: " + targetActivity.getSimpleName(), e);
            Toast.makeText(this, "Error: Unable to navigate to " + targetActivity.getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToChat() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference groupChatRef = FirebaseDatabase.getInstance().getReference("groupChats");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();

            // Fetch the current user's selectedCarId
            usersRef.child(currentUserId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String selectedCarId = snapshot.child("selectedCarId").getValue(String.class);
                    if (selectedCarId != null) {
                        // Find or create a group chat for the car ID
                        usersRef.orderByChild("selectedCarId").equalTo(selectedCarId).get()
                                .addOnSuccessListener(userSnapshot -> {
                                    List<String> userIds = new ArrayList<>();
                                    for (DataSnapshot user : userSnapshot.getChildren()) {
                                        userIds.add(user.getKey());
                                    }

                                    if (userIds.size() > 1) {
                                        // Check if the group already exists
                                        groupChatRef.get().addOnSuccessListener(groupSnapshot -> {
                                            boolean groupExists = false;
                                            String existingGroupId = null;

                                            for (DataSnapshot group : groupSnapshot.getChildren()) {
                                                GroupChatActivity.GroupChat groupChat = group.getValue(GroupChatActivity.GroupChat.class);

                                                if (groupChat != null && groupChat.members.containsAll(userIds) && userIds.containsAll(groupChat.members)) {
                                                    groupExists = true;
                                                    existingGroupId = groupChat.groupId;
                                                    break;
                                                }
                                            }

                                            if (groupExists) {
                                                // Navigate to the existing chat
                                                Log.d(TAG, "Navigating to existing chat with groupId: " + existingGroupId);
                                                Intent intent = new Intent(this, ChatActivity.class);
                                                intent.putExtra("groupId", existingGroupId);
                                                startActivity(intent);
                                            } else {
                                                // Create a new group chat
                                                String groupId = groupChatRef.push().getKey();
                                                GroupChatActivity.GroupChat newGroupChat = new GroupChatActivity.GroupChat(groupId, userIds, System.currentTimeMillis());

                                                groupChatRef.child(groupId).setValue(newGroupChat)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "Created new chat with groupId: " + groupId);
                                                            Intent intent = new Intent(this, ChatActivity.class);
                                                            intent.putExtra("groupId", groupId);
                                                            startActivity(intent);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.e(TAG, "Failed to create chat: " + e.getMessage());
                                                            Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "No other users share your car ID.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    Log.e(TAG, "Error fetching users: " + e.getMessage());
                                    Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No car assigned to your account.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch your car ID: " + e.getMessage());
                Toast.makeText(this, "Failed to fetch your car ID.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(0, 0); // Disable animations globally
    }
}
