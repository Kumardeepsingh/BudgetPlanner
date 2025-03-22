package com.example.budgetplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class BudgetSettingActivity extends AppCompatActivity {

    // UI components
    private TextView textViewCurrentMonth;
    private EditText editTextBudgetAmount;
    private Button buttonSaveBudget;

    // Database handler
    private DbHandler dbHandler;

    // Current year-month
    private String currentYearMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting);

        // Initialize database handler
        dbHandler = new DbHandler(this);

        // Get current year-month
        currentYearMonth = dbHandler.getCurrentYearMonth();

        // Initialize UI components
        initializeViews();

        // Load current budget settings
        loadBudgetSettings();

        // Set up click listener for save button
        buttonSaveBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBudgetSettings();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_navigation_bills) {
            Intent intent = new Intent(BudgetSettingActivity.this, BillManagementActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(BudgetSettingActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Toggle menu items based on current view
        menu.findItem(R.id.menu_view_history).setVisible(false);
        menu.findItem(R.id.menu_current_period).setVisible(false);
        menu.findItem(R.id.menu_budget_settings).setVisible(false);


        return super.onPrepareOptionsMenu(menu);
    }

    private void initializeViews() {
        textViewCurrentMonth = findViewById(R.id.textViewCurrentMonth);
        editTextBudgetAmount = findViewById(R.id.editTextBudgetAmount);
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget);

        // Format and display current month
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        textViewCurrentMonth.setText("Budget for: " + monthYearFormat.format(calendar.getTime()));
    }

    private void loadBudgetSettings() {
        // Get current budget for this month
        Budget currentBudget = dbHandler.getBudget(currentYearMonth);

        if (currentBudget != null) {
            // Display current budget amount
            editTextBudgetAmount.setText(String.format(Locale.getDefault(), "%.2f", currentBudget.getAmount()));
        } else {
            // No budget set for this month, check if we should copy from previous month
            // or set default
            editTextBudgetAmount.setText("1000.00");
        }
    }

    private void saveBudgetSettings() {
        // Validate input
        String budgetAmountStr = editTextBudgetAmount.getText().toString().trim();
        if (budgetAmountStr.isEmpty()) {
            editTextBudgetAmount.setError("Please enter a budget amount");
            return;
        }

        // Parse budget amount
        double budgetAmount;
        try {
            budgetAmount = Double.parseDouble(budgetAmountStr);
            if (budgetAmount < 0) {
                editTextBudgetAmount.setError("Budget amount must be non-negative");
                return;
            }
        } catch (NumberFormatException e) {
            editTextBudgetAmount.setError("Invalid budget amount");
            return;
        }

        // Create budget object and save to database
        Budget budget = new Budget(currentYearMonth, budgetAmount);
        dbHandler.setBudget(budget);

        // Show success message
        Toast.makeText(this, "Budget settings saved for " +
                        textViewCurrentMonth.getText().toString().replace("Budget for: ", ""),
                Toast.LENGTH_SHORT).show();

        // Close activity
        finish();
    }
}