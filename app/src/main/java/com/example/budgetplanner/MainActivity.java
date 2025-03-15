package com.example.budgetplanner;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI components
    private TextView textViewIncome;
    private TextView textViewExpenses;
    private TextView textViewBalance;
    private ProgressBar progressBarBudget;
    private TextView textViewBudgetTotal;
    private ListView listViewTransactions;
    private TextView textViewNoTransactions;
    private Button buttonAddExpense;
    private Button buttonAddIncome;

    // Adapter
    private TransactionAdapter transactionAdapter;

    // Sample data (will be replaced with actual database data later)
    private double totalIncome = 0.00;
    private double totalExpenses = 0.00;
    private double budgetAmount = 1000.00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Set up click listeners
        setupClickListeners();

        // Update UI with data
        updateSummaryData();

        // For now, we'll use sample data for transactions
        displayTransactions();
    }

    private void initializeViews() {
        textViewIncome = findViewById(R.id.textViewIncome);
        textViewExpenses = findViewById(R.id.textViewExpenses);
        textViewBalance = findViewById(R.id.textViewBalance);
        progressBarBudget = findViewById(R.id.progressBarBudget);
        textViewBudgetTotal = findViewById(R.id.textViewBudgetTotal);
        listViewTransactions = findViewById(R.id.listViewTransactions);
        textViewNoTransactions = findViewById(R.id.textViewNoTransactions);
        buttonAddExpense = findViewById(R.id.buttonAddExpense);
        buttonAddIncome = findViewById(R.id.buttonAddIncome);

        // Set up ListView item click listener
        listViewTransactions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Transaction transaction = (Transaction) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, "Clicked: " + transaction.getCategory(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        buttonAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);


            }
        });

        buttonAddIncome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, AddIncomeActivity.class);
                startActivity(intent);


            }
        });
    }

    private void updateSummaryData() {
        // Calculate totals from transaction list (this will be from database later)
        List<Transaction> transactions = getTransactionList();
        totalIncome = 0;
        totalExpenses = 0;

        for (Transaction transaction : transactions) {
            if (transaction.isIncome()) {
                totalIncome += transaction.getAmount();
            } else {
                totalExpenses += transaction.getAmount();
            }
        }

        // Update income, expenses, and balance text views
        textViewIncome.setText(String.format("$%.2f", totalIncome));
        textViewExpenses.setText(String.format("$%.2f", totalExpenses));
        double balance = totalIncome - totalExpenses;
        textViewBalance.setText(String.format("$%.2f", balance));

        // Update budget progress bar
        int budgetPercentage = budgetAmount > 0 ? (int)((totalExpenses / budgetAmount) * 100) : 0;
        progressBarBudget.setProgress(Math.min(budgetPercentage, 100));

        // Update budget text
        textViewBudgetTotal.setText(String.format("$%.2f budget", budgetAmount));
    }

    private void displayTransactions() {
        List<Transaction> transactions = getTransactionList();

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

    @Override
    protected void onResume() {
        super.onResume();
        // We'll refresh data from the database here when we implement it
        // For now, just call our update methods
        updateSummaryData();
        displayTransactions();
    }

    // This is just for sample data, will be replaced with database queries
    private List<Transaction> getTransactionList() {
        List<Transaction> transactions = new ArrayList<>();

        // Create a date formatter for parsing sample dates
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            // Add some sample transactions
            transactions.add(new Transaction("income", 2500.00, "Salary", dateFormat.parse("2025-03-01"), "Monthly salary"));
            transactions.add(new Transaction("expense", 800.00, "Rent", dateFormat.parse("2025-03-03"), "March rent"));
            transactions.add(new Transaction("expense", 120.50, "Groceries", dateFormat.parse("2025-03-05"), "Weekly shopping"));
            transactions.add(new Transaction("expense", 45.00, "Dining", dateFormat.parse("2025-03-08"), "Dinner with friends"));
            transactions.add(new Transaction("expense", 35.00, "Transportation", dateFormat.parse("2025-03-10"), "Gas"));
            transactions.add(new Transaction("income", 200.00, "Freelance", dateFormat.parse("2025-03-12"), "Logo design project"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return transactions;
    }
}