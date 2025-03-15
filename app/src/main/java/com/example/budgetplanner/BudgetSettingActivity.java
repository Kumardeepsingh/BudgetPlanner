package com.example.budgetplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BudgetSettingActivity extends AppCompatActivity {

    // UI components
    private EditText editTextBudgetAmount;
    private RadioGroup radioGroupBudgetPeriod;
    private RadioButton radioButtonMonthly;
    private RadioButton radioButtonWeekly;
    private Button buttonSaveBudget;

    // Constants for SharedPreferences
    public static final String PREFS_NAME = "BudgetPrefs";
    public static final String PREF_BUDGET_AMOUNT = "budget_amount";
    public static final String PREF_BUDGET_PERIOD = "budget_period";
    public static final String PERIOD_MONTHLY = "monthly";
    public static final String PERIOD_WEEKLY = "weekly";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_setting);

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

    private void initializeViews() {
        editTextBudgetAmount = findViewById(R.id.editTextBudgetAmount);
        radioGroupBudgetPeriod = findViewById(R.id.radioGroupBudgetPeriod);
        radioButtonMonthly = findViewById(R.id.radioButtonMonthly);
        radioButtonWeekly = findViewById(R.id.radioButtonWeekly);
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget);
    }

    private void loadBudgetSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get budget amount with default of 1000.00
        double budgetAmount = prefs.getFloat(PREF_BUDGET_AMOUNT, 1000.00f);
        editTextBudgetAmount.setText(String.format("%.2f", budgetAmount));

        // Get budget period with default of monthly
        String budgetPeriod = prefs.getString(PREF_BUDGET_PERIOD, PERIOD_MONTHLY);
        if (budgetPeriod.equals(PERIOD_WEEKLY)) {
            radioButtonWeekly.setChecked(true);
        } else {
            radioButtonMonthly.setChecked(true);
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
            if (budgetAmount <= 0) {
                editTextBudgetAmount.setError("Budget amount must be greater than zero");
                return;
            }
        } catch (NumberFormatException e) {
            editTextBudgetAmount.setError("Invalid budget amount");
            return;
        }

        // Get selected budget period
        String budgetPeriod = PERIOD_MONTHLY; // default
        int selectedRadioId = radioGroupBudgetPeriod.getCheckedRadioButtonId();
        if (selectedRadioId == R.id.radioButtonWeekly) {
            budgetPeriod = PERIOD_WEEKLY;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(PREF_BUDGET_AMOUNT, (float) budgetAmount);
        editor.putString(PREF_BUDGET_PERIOD, budgetPeriod);
        editor.apply();

        // Show success message
        Toast.makeText(this, "Budget settings saved", Toast.LENGTH_SHORT).show();

        // Close activity
        finish();
    }
}