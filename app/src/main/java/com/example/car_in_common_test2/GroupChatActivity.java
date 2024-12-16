package com.example.car_in_common_test2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GroupChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Button createGroupButton = findViewById(R.id.createGroupButton);
        Button joinGroupButton = findViewById(R.id.joinGroupButton);

        createGroupButton.setOnClickListener(v -> {
            Toast.makeText(this, "Create Group clicked", Toast.LENGTH_SHORT).show();
            // Navigate to CreateGroupActivity here
        });

        joinGroupButton.setOnClickListener(v -> {
            Toast.makeText(this, "Join Group clicked", Toast.LENGTH_SHORT).show();
            // Navigate to JoinGroupActivity here
        });

    }
}
