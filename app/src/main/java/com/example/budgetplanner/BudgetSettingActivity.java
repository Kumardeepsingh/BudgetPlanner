package com.example.budgetplanner;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BudgetSettingActivity extends AppCompatActivity {

    // UI components
    private EditText editTextBudgetAmount;
    private EditText editTextStartDate, editTextEndDate;

    private Button buttonSaveBudget;

    // Constants for SharedPreferences
    public static final String PREFS_NAME = "BudgetPrefs";
    public static final String PREF_BUDGET_AMOUNT = "budget_amount";
    public static final String PREF_BUDGET_PERIOD = "budget_period";
    public static final String PREF_BUDGET_START_DATE = "budget_start_date";
    public static final String PREF_BUDGET_END_DATE = "budget_end_date";



    public static final String PREF_LAST_RESET_DATE = "last_reset_date";


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
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate = findViewById(R.id.editTextEndDate);

        editTextStartDate.setOnClickListener(v -> showDatePicker(editTextStartDate));
        editTextEndDate.setOnClickListener(v -> showDatePicker(editTextEndDate));
        buttonSaveBudget = findViewById(R.id.buttonSaveBudget);
    }
    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            editText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadBudgetSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get budget amount with default of 1000.00
        double budgetAmount = prefs.getFloat(PREF_BUDGET_AMOUNT, 1000.00f);
        editTextBudgetAmount.setText(String.format("%.2f", budgetAmount));

        // Get start date and end date
        String startDate = prefs.getString(BudgetSettingActivity.PREF_BUDGET_START_DATE, "");
        String endDate = prefs.getString(BudgetSettingActivity.PREF_BUDGET_END_DATE, "");

        // Populate start and end date fields if they exist
        if (!startDate.isEmpty()) {
            editTextStartDate.setText(startDate);
        }

        if (!endDate.isEmpty()) {
            editTextEndDate.setText(endDate);
        }
    }

    private void saveBudgetSettings() {
        String budgetAmountStr = editTextBudgetAmount.getText().toString().trim();
        if (budgetAmountStr.isEmpty()) {
            editTextBudgetAmount.setError("Please enter a budget amount");
            return;
        }

        double budgetAmount = Double.parseDouble(budgetAmountStr);
        if (budgetAmount <= 0) {
            editTextBudgetAmount.setError("Budget amount must be greater than zero");
            return;
        }

        String startDate = editTextStartDate.getText().toString().trim();
        String endDate = editTextEndDate.getText().toString().trim();
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putFloat(PREF_BUDGET_AMOUNT, (float) budgetAmount);
        editor.putString(PREF_BUDGET_START_DATE, startDate);
        editor.putString(PREF_BUDGET_END_DATE, endDate);
        editor.apply();

        Toast.makeText(this, "Budget settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }

}