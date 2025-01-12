package com.example.car_in_common_test2;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionsActivity extends BaseActivity {

    private double totalExpenses = 0.0;
    private TextView totalExpensesView;
    private ArrayList<String> expenseHistory;

    // Firebase references
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_dapanes, findViewById(R.id.contentFrame));

        totalExpensesView = findViewById(R.id.total_expenses);
        Button addExpenseButton = findViewById(R.id.add_expense_button);
        Button viewHistoryButton = findViewById(R.id.view_history_button);

        expenseHistory = new ArrayList<>();

        // Initialize Firebase database references
        databaseReference = FirebaseDatabase.getInstance().getReference("expenses");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        addExpenseButton.setOnClickListener(v -> showAddExpenseDialog());
        viewHistoryButton.setOnClickListener(v -> showExpenseHistoryDialog());

        // Load initial expense data from Firebase
        loadExpensesFromFirebase();
    }

    private void showExpenseHistoryDialog() {
        // Create a dialog using a custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Expense History");

        if (expenseHistory.isEmpty()) {
            builder.setMessage("No expenses recorded.");
        } else {
            // Convert the expense history list to a string array for the dialog
            String[] expenseArray = expenseHistory.toArray(new String[0]);

            builder.setItems(expenseArray, null);
        }

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAddExpenseDialog() {
        // Create a dialog using a custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_add_expense, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Find views in the custom layout
        EditText expenseAmountInput = dialogView.findViewById(R.id.expense_amount);
        EditText expenseDateInput = dialogView.findViewById(R.id.expense_date);
        Spinner expenseReasonSpinner = dialogView.findViewById(R.id.expense_reason_spinner);
        CheckBox shareExpenseCheckBox = dialogView.findViewById(R.id.share_expense_checkbox);
        Button declareExpenseButton = dialogView.findViewById(R.id.declare_expense_button);

        // Populate the spinner with options
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_reasons,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expenseReasonSpinner.setAdapter(adapter);

        declareExpenseButton.setOnClickListener(v -> {
            String amountText = expenseAmountInput.getText().toString().trim();
            String dateText = expenseDateInput.getText().toString().trim();
            String reasonText = expenseReasonSpinner.getSelectedItem().toString();
            boolean shareExpense = shareExpenseCheckBox.isChecked();

            if (amountText.isEmpty() || dateText.isEmpty() || reasonText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double expenseAmount = Double.parseDouble(amountText);

                if (shareExpense) {
                    // Share the expense with users who have the same car ID
                    String carId = "your_car_id_here"; // Replace with actual car ID logic
                    findUsersWithSameCarId(carId, expenseAmount);
                } else {
                    // Update total expenses and UI
                    totalExpenses += expenseAmount;
                    totalExpensesView.setText(String.format("Total Expenses: $%.2f", totalExpenses));

                    // Add to expense history
                    String expenseDetail = String.format("Amount: $%.2f, Date: %s, Reason: %s", expenseAmount, dateText, reasonText);
                    expenseHistory.add(expenseDetail);

                    // Save to Firebase
                    saveExpenseToFirebase(expenseAmount, dateText, reasonText);
                }

                Toast.makeText(this, "Expense Added Successfully!", Toast.LENGTH_SHORT).show();

                // Close the dialog
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount. Please enter a number.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveExpenseToFirebase(double amount, String date, String reason) {
        String key = databaseReference.push().getKey();
        if (key != null) {
            Map<String, Object> expense = new HashMap<>();
            expense.put("amount", amount);
            expense.put("date", date);
            expense.put("reason", reason);

            databaseReference.child(key).setValue(expense)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Expense saved successfully."))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to save expense.", e));
        }
    }

    private void loadExpensesFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseHistory.clear();
                totalExpenses = 0.0;

                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    double amount = expenseSnapshot.child("amount").getValue(Double.class);
                    String date = expenseSnapshot.child("date").getValue(String.class);
                    String reason = expenseSnapshot.child("reason").getValue(String.class);

                    if (date != null && reason != null) {
                        String expenseDetail = String.format("Amount: $%.2f, Date: %s, Reason: %s", amount, date, reason);
                        expenseHistory.add(expenseDetail);
                        totalExpenses += amount;
                    }
                }

                // Update the total expenses view
                totalExpensesView.setText(String.format("Total Expenses: $%.2f", totalExpenses));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load expenses.", error.toException());
            }
        });
    }

    private void findUsersWithSameCarId(String carId, double sharedAmount) {
        if (carId == null || carId.isEmpty()) {
            Toast.makeText(this, "Car ID cannot be null or empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("selectedCarId").equalTo(carId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        long userCount = snapshot.getChildrenCount(); // Get the count of matching users

                        if (userCount > 1) {
                            List<String> userIds = new ArrayList<>();
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String userId = userSnapshot.getKey();
                                if (userId != null) {
                                    userIds.add(userId);
                                }
                            }

                            double amountPerUser = sharedAmount / userCount;

                            for (String userId : userIds) {
                                notifyUser(userId, amountPerUser);
                            }

                            Toast.makeText(this, "Expense shared successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No other users share your car ID. Total users: " + userCount, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No users found with this car ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DatabaseError", "Error fetching users: " + e.getMessage());
                    Toast.makeText(this, "Error fetching users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void notifyUser(String userId, double amount) {
        // Notify the user of the shared expense
        DatabaseReference userNotificationsRef = usersRef.child(userId).child("notifications");

        Map<String, Object> notification = new HashMap<>();
        notification.put("message", "You owe $" + String.format("%.2f", amount) + " for a shared expense.");
        notification.put("timestamp", System.currentTimeMillis());

        userNotificationsRef.push().setValue(notification)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "Notification sent to user: " + userId))
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to send notification to user: " + userId, e));
    }
}
