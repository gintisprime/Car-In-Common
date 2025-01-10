package com.example.car_in_common_test2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

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




        // Set navigation listeners
        navHome.setOnClickListener(v -> navigateTo(MainMenuActivity.class));
        navCalendar.setOnClickListener(v -> navigateTo(CalendarActivity.class));
        navMaps.setOnClickListener(v -> navigateTo(MapsActivity.class));
        navTransactions.setOnClickListener(v -> navigateTo(TransactionsActivity.class));
        navChat.setOnClickListener(v -> navigateToChat());
    }

    private void navigateTo(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) { // Avoid reloading the current activity
            startActivity(new Intent(this, targetActivity));
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
                                                Intent intent = new Intent(this, ChatActivity.class);
                                                intent.putExtra("groupId", existingGroupId);
                                                startActivity(intent);
                                            } else {
                                                // Create a new group chat
                                                String groupId = groupChatRef.push().getKey();
                                                GroupChatActivity.GroupChat newGroupChat = new GroupChatActivity.GroupChat(groupId, userIds, System.currentTimeMillis());

                                                groupChatRef.child(groupId).setValue(newGroupChat)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Intent intent = new Intent(this, ChatActivity.class);
                                                            intent.putExtra("groupId", groupId);
                                                            startActivity(intent);
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "No other users share your car ID.", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No car assigned to your account.", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(e -> {
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
