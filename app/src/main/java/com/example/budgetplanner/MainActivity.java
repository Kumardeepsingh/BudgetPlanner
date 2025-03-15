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

import java.util.List;

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

    // Database helper
    private DbHandler dbHelper;

    // Budget amount (can be moved to settings later)
    private double budgetAmount = 1000.00;

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

                // TODO: Add transaction detail view or edit functionality
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
        // Get totals from database
        double totalIncome = dbHelper.getTotalIncome();
        double totalExpenses = dbHelper.getTotalExpenses();

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
        List<Transaction> transactions = dbHelper.getAllTransactions();

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
        // Refresh data from the database
        updateSummaryData();
        displayTransactions();
    }
}