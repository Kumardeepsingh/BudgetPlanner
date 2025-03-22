package com.example.budgetplanner;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class AddBillActivity extends AppCompatActivity {


    private EditText editTextBillName, editTextBillAmount, editTextBillDate, editTextBillDescription;
    private Button buttonSaveBill;
    private DbHandler dbHandler;
    private Calendar calendar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_bill);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Components
        dbHandler = new DbHandler(this);
        editTextBillName = findViewById(R.id.editTextBillName);
        editTextBillAmount = findViewById(R.id.editTextBillAmount);
        editTextBillDate = findViewById(R.id.editTextBillDate);
        editTextBillDescription = findViewById(R.id.editTextBillDescription);
        buttonSaveBill = findViewById(R.id.buttonSaveBill);

        // Open Date Picker when clicking on Bill Date field
        editTextBillDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle Save Bill Button Click
        buttonSaveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBill();
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

        if (itemId == R.id.menu_budget_settings) {
            Intent intent = new Intent(AddBillActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.menu_navigation_bills) {
            Intent intent = new Intent(AddBillActivity.this, BillManagementActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(AddBillActivity.this, MainActivity.class);
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

    private void showDatePicker() {
        calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            editTextBillDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void saveBill() {
        String billName = editTextBillName.getText().toString().trim();
        String amountStr = editTextBillAmount.getText().toString().trim();
        String dueDate = editTextBillDate.getText().toString().trim();
        String description = editTextBillDescription.getText().toString().trim();

        if (billName.isEmpty() || amountStr.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        long billID = dbHandler.addBill(billName, amount, dueDate, description);

        if (billID != -1) {
            Toast.makeText(this, "Bill added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddBillActivity.this, BillManagementActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Prevents stack duplicates
            startActivity(intent);

        } else {
            Toast.makeText(this, "Failed to add bill", Toast.LENGTH_SHORT).show();
        }
    }

}
