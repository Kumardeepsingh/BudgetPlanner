package com.example.budgetplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI components
    private TextView textViewIncome;
    private TextView textViewExpenses;
    private TextView textViewBalance;
    private ProgressBar progressBarBudget;
    private TextView textViewBudgetTotal;
    private TextView textViewSpent;
    private ListView listViewTransactions;
    private TextView textViewNoTransactions;
    private TextView txtTimePeriod;
    private Button buttonAddExpense;
    private Button buttonAddIncome;

    // Adapter
    private TransactionAdapter transactionAdapter;

    // Database helper
    private DbHandler dbHelper;

    // Budget amount and period
    private double budgetAmount = 1000.00;
    private String budgetStartDate, budgetEndDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database helper
        dbHelper = new DbHandler(this);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Load budget settings
        loadBudgetSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_budget_settings) {
            Intent intent = new Intent(MainActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeViews() {
        textViewIncome = findViewById(R.id.textViewIncome);
        textViewExpenses = findViewById(R.id.textViewExpenses);
        textViewBalance = findViewById(R.id.textViewBalance);
        progressBarBudget = findViewById(R.id.progressBarBudget);
        textViewBudgetTotal = findViewById(R.id.textViewBudgetTotal);
        textViewSpent = findViewById(R.id.textViewSpent);
        listViewTransactions = findViewById(R.id.listViewTransactions);
        textViewNoTransactions = findViewById(R.id.textViewNoTransactions);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddIncome = findViewById(R.id.buttonAddIncome);
        txtTimePeriod = findViewById(R.id.txtTimePeriod);

        // Set up ListView item click listener
        listViewTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction transaction = (Transaction) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();

                // TODO: Add transaction detail view or edit functionality
            }
        });
    }

    private void setupClickListeners() {
        buttonAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivityForResult(intent, 1);
        });

        buttonAddIncome.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddIncomeActivity.class);
            startActivityForResult(intent, 2);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Log.d("MainActivity", "Refreshing after adding transaction...");
            loadBudgetSettings();
            updateSummaryData();
            displayTransactions();
        }
    }


    private void loadBudgetSettings() {
        SharedPreferences prefs = getSharedPreferences(BudgetSettingActivity.PREFS_NAME, MODE_PRIVATE);
        budgetAmount = prefs.getFloat(BudgetSettingActivity.PREF_BUDGET_AMOUNT, 1000.00f);
        // Get budget start and end date
        budgetStartDate = prefs.getString(BudgetSettingActivity.PREF_BUDGET_START_DATE, "");
        budgetEndDate = prefs.getString(BudgetSettingActivity.PREF_BUDGET_END_DATE, "");
    }

    private void updateSummaryData() {

        // Get totals from database
        //double totalIncome = dbHelper.getTotalIncome();
        double totalIncome = dbHelper.getTotalIncomeForPeriod(budgetStartDate, budgetEndDate);
        //double totalExpenses = dbHelper.getTotalExpenses();
        double totalExpenses = dbHelper.getTotalExpensesForPeriod(budgetStartDate, budgetEndDate);

        // Update income, expenses, and balance text views
        textViewIncome.setText(String.format("$%.2f", totalIncome));
        textViewExpenses.setText(String.format("$%.2f", totalExpenses));
        txtTimePeriod.setText("("+budgetStartDate+" - " + budgetEndDate+")");
        double balance = totalIncome - totalExpenses;
        textViewBalance.setText(String.format("$%.2f", balance));

        int budgetPercentage = budgetAmount > 0 ? (int) ((totalExpenses / budgetAmount) * 100) : 0;
        progressBarBudget.setProgress(Math.min(budgetPercentage, 100));

        textViewBudgetTotal.setText(String.format("$%.2f budget ", budgetAmount));
        textViewSpent.setText(String.format("$%.2f spent", totalExpenses));


    }

    private void displayTransactions() {

        budgetStartDate = budgetStartDate.trim(); // Remove spaces or hidden characters
        budgetEndDate = budgetEndDate.trim();


        List<Transaction> transactions = dbHelper.getAllTransactions(budgetStartDate, budgetEndDate);
        //List<Transaction> transactions = dbHelper.getAllTransactions("2025-03-18", "2025-03-22");

        if (transactions.isEmpty()) {
            textViewNoTransactions.setVisibility(View.VISIBLE);
            listViewTransactions.setVisibility(View.GONE);
        } else {
            textViewNoTransactions.setVisibility(View.GONE);
            listViewTransactions.setVisibility(View.VISIBLE);

            // Initialize adapter if needed
            if (transactionAdapter == null) {
                transactionAdapter = new TransactionAdapter(this, transactions);
                listViewTransactions.setAdapter(transactionAdapter);
            } else {
                transactionAdapter.updateData(transactions);
            }
        }
    }

    private void checkAndResetBudget() {
        SharedPreferences prefs = getSharedPreferences(BudgetSettingActivity.PREFS_NAME, MODE_PRIVATE);
        String endDateStr = prefs.getString(BudgetSettingActivity.PREF_BUDGET_END_DATE, "");

        if (!endDateStr.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date endDate = sdf.parse(endDateStr);
                Date today = new Date();

                if (today.after(endDate)) {
                    showBudgetExpiredDialog();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    private void showBudgetExpiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Budget Period Ended");
        builder.setMessage("Your budget period has ended. Would you like to set a new budget?");

        builder.setPositiveButton("Set New Budget", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
        });

        builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    

    @Override
    protected void onResume() {
        super.onResume();
        // Reload budget settings
        loadBudgetSettings();
        checkAndResetBudget();
        // Refresh data from the database
        updateSummaryData();
        displayTransactions();
    }
}