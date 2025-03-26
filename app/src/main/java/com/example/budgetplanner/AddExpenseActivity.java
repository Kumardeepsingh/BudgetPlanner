package com.example.budgetplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    private EditText editTextAmount;
    private Spinner spinnerCategory;
    private EditText editTextDate;
    private EditText editTextDescription;
    private Button buttonSaveExpense;

    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private Date selectedDate;

    // Database helper
    private DbHandler dbHelper;

    // Sample categories for expenses
    private final String[] expenseCategories = {
            "Food & Dining", "Groceries", "Transportation", "Rent", "Utilities",
            "Entertainment", "Shopping", "Healthcare", "Education", "Personal Care",
            "Travel", "Gifts & Donations", "Bills", "Insurance", "Taxes", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        // Initialize database helper
        dbHelper = new DbHandler(this);

        // Initialize date formatter
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        calendar = Calendar.getInstance();
        selectedDate = calendar.getTime();

        // Initialize views
        initializeViews();

        // Set up category dropdown
        setupCategoryDropdown();

        // Set up date picker
        setupDatePicker();

        // Set up save button
        setupSaveButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_budget_settings) {
            Intent intent = new Intent(AddExpenseActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.menu_navigation_bills) {
            Intent intent = new Intent(AddExpenseActivity.this, BillManagementActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(AddExpenseActivity.this, MainActivity.class);
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

        return super.onPrepareOptionsMenu(menu);
    }

    private void initializeViews() {
        editTextAmount = findViewById(R.id.editTextAmount);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        editTextDate = findViewById(R.id.editTextDate);
        editTextDescription = findViewById(R.id.editTextDescription);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);

        // Set current date as default
        editTextDate.setText(dateFormatter.format(selectedDate));
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, expenseCategories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);
    }

    private void setupDatePicker() {
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        selectedDate = calendar.getTime();
                        editTextDate.setText(dateFormatter.format(selectedDate));
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupSaveButton() {
        buttonSaveExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExpense();
            }
        });
    }

    private void saveExpense() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Get values from inputs
        double amount = Double.parseDouble(editTextAmount.getText().toString().trim());
        String category = spinnerCategory.getSelectedItem().toString();
        String description = editTextDescription.getText().toString().trim();

        // Create a new transaction object
        Transaction transaction = new Transaction(
                "expense",
                amount,
                category,
                selectedDate,
                description
        );

        // Save transaction to database
        long id = dbHelper.addTransaction(transaction);

        if (id != -1) {
            // Successfully inserted
            Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Failed to insert
            Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Validate amount
        String amountStr = editTextAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            editTextAmount.setError("Amount is required");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    editTextAmount.setError("Amount must be greater than 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                editTextAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        // Validate category - not needed for Spinner as it always has a selection
        // But we can check if a valid category is selected if needed

        return isValid;
    }
}