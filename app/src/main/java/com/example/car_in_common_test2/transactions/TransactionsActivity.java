package com.example.car_in_common_test2.transactions;

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

import com.example.car_in_common_test2.R;
import com.example.car_in_common_test2.utils.BaseActivity;
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

    private static final String TAG = "TransactionsActivity";

    private double totalExpenses = 0.0;
    private TextView totalExpensesView;
    private List<String> expenseHistory;

    // Firebase references
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the specific layout into BaseActivity's content frame
        getLayoutInflater().inflate(R.layout.activity_dapanes, findViewById(R.id.contentFrame), true);

        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().getReference("expenses");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI components
        totalExpensesView = findViewById(R.id.total_expenses);
        Button addExpenseButton = findViewById(R.id.add_expense_button);
        Button viewHistoryButton = findViewById(R.id.view_history_button);

        // Validate UI components to avoid null pointers
        if (totalExpensesView == null || addExpenseButton == null || viewHistoryButton == null) {
            throw new IllegalStateException("Failed to initialize one or more views from activity_dapanes.xml");
        }

        // Initialize expense history
        expenseHistory = new ArrayList<>();

        // Set up button listeners
        addExpenseButton.setOnClickListener(v -> showAddExpenseDialog());
        viewHistoryButton.setOnClickListener(v -> showExpenseHistoryDialog());

        // Load initial expense data from Firebase
        loadExpensesFromFirebase();
    }

    private void loadExpensesFromFirebase() {
        if (databaseReference == null) {
            Log.e(TAG, "DatabaseReference is null. Ensure Firebase is properly initialized.");
            Toast.makeText(this, "Error: Could not connect to Firebase.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expenseHistory.clear();
                totalExpenses = 0.0;

                for (DataSnapshot expenseSnapshot : snapshot.getChildren()) {
                    Double amount = expenseSnapshot.child("amount").getValue(Double.class);
                    String date = expenseSnapshot.child("date").getValue(String.class);
                    String reason = expenseSnapshot.child("reason").getValue(String.class);

                    if (amount != null && date != null && reason != null) {
                        String expenseDetail = String.format("Amount: $%.2f, Date: %s, Reason: %s", amount, date, reason);
                        expenseHistory.add(expenseDetail);
                        totalExpenses += amount;
                    }
                }

                totalExpensesView.setText(String.format("Total Expenses: $%.2f", totalExpenses));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load expenses.", error.toException());
                Toast.makeText(TransactionsActivity.this, "Failed to load expenses.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showExpenseHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Expense History");

        if (expenseHistory.isEmpty()) {
            builder.setMessage("No expenses recorded.");
        } else {
            String[] expenseArray = expenseHistory.toArray(new String[0]);
            builder.setItems(expenseArray, null);
        }

        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void showAddExpenseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.popup_add_expense, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Initialize dialog components
        EditText expenseAmountInput = dialogView.findViewById(R.id.expense_amount);
        EditText expenseDateInput = dialogView.findViewById(R.id.expense_date);
        Spinner expenseReasonSpinner = dialogView.findViewById(R.id.expense_reason_spinner);
        CheckBox shareExpenseCheckBox = dialogView.findViewById(R.id.share_expense_checkbox);
        Button declareExpenseButton = dialogView.findViewById(R.id.declare_expense_button);

        // Populate spinner options
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

            if (amountText.isEmpty() || dateText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double expenseAmount = Double.parseDouble(amountText);
                if (shareExpense) {
                    String carId = "your_car_id_here"; // Replace with actual car ID logic
                    findUsersWithSameCarId(carId, expenseAmount);
                } else {
                    totalExpenses += expenseAmount;
                    totalExpensesView.setText(String.format("Total Expenses: $%.2f", totalExpenses));
                    String expenseDetail = String.format("Amount: $%.2f, Date: %s, Reason: %s", expenseAmount, dateText, reasonText);
                    expenseHistory.add(expenseDetail);
                    saveExpenseToFirebase(expenseAmount, dateText, reasonText);
                }

                Toast.makeText(this, "Expense added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid amount. Please enter a number.", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void saveExpenseToFirebase(double amount, String date, String reason) {
        if (databaseReference == null) {
            Log.e(TAG, "DatabaseReference is null. Cannot save expense.");
            return;
        }

        String key = databaseReference.push().getKey();
        if (key != null) {
            Map<String, Object> expense = new HashMap<>();
            expense.put("amount", amount);
            expense.put("date", date);
            expense.put("reason", reason);

            databaseReference.child(key).setValue(expense)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Expense saved successfully."))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save expense.", e));
        }
    }

    private void findUsersWithSameCarId(String carId, double sharedAmount) {
        usersRef.orderByChild("selectedCarId").equalTo(carId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        List<String> userIds = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String userId = userSnapshot.getKey();
                            if (userId != null) userIds.add(userId);
                        }

                        if (userIds.size() > 1) {
                            double amountPerUser = sharedAmount / userIds.size();
                            for (String userId : userIds) {
                                notifyUser(userId, amountPerUser);
                            }
                            Toast.makeText(this, "Expense shared successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No other users share your car ID.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No users found with this car ID.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error finding users.", e));
    }

    private void notifyUser(String userId, double amount) {
        DatabaseReference userNotificationsRef = usersRef.child(userId).child("notifications");

        Map<String, Object> notification = new HashMap<>();
        notification.put("message", "You owe $" + String.format("%.2f", amount) + " for a shared expense.");
        notification.put("timestamp", System.currentTimeMillis());

        userNotificationsRef.push().setValue(notification)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification sent to user: " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to send notification.", e));
    }
}
