package com.example.budgetplanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private Button buttonAddExpense;
    private Button buttonAddIncome;

    private Button buttonPrevPeriod;
    private Button buttonNextPeriod;

    // Adapter
    private TransactionAdapter transactionAdapter;

    // Database helper
    private DbHandler dbHelper;

    // Budget amount and period
    private double budgetAmount = 1000.00;
    private int periodOffset = 0; // 0 = current period, -1 = previous period, etc.
    private boolean isViewingHistory = false;

    private static final String PREFS_NAME = "BudgetPrefs";
    private static final String PREF_BUDGET_ALERT_SHOWN = "budget_alert_shown";

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
        int itemId = item.getItemId();

        if (itemId == R.id.menu_budget_settings) {
            Intent intent = new Intent(MainActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.menu_navigation_bills) {
            Intent intent = new Intent(MainActivity.this, BillManagementActivity.class);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.menu_view_history) {
            // Switch to history view
            isViewingHistory = true;
            periodOffset = -1; // Start with previous period
            updatePeriodNavigationVisibility();
            updateSummaryTitle();
            loadBudgetSettings(); // Load budget for previous period
            loadTransactionsForPeriod();
            invalidateOptionsMenu(); // Refresh menu to show/hide items
            return true;
        } else if (itemId == R.id.menu_current_period) {
            // Switch back to current period view
            isViewingHistory = false;
            periodOffset = 0;
            updatePeriodNavigationVisibility();
            updateSummaryTitle();
            loadBudgetSettings(); // Load budget for current period
            loadTransactionsForPeriod();
            invalidateOptionsMenu(); // Refresh menu to show/hide items
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Toggle menu items based on current view
        menu.findItem(R.id.menu_view_history).setVisible(!isViewingHistory);
        menu.findItem(R.id.menu_current_period).setVisible(isViewingHistory);
        menu.findItem(R.id.menu_home).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
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

        // Initialize period navigation buttons
        buttonPrevPeriod = findViewById(R.id.buttonPrevPeriod);
        buttonNextPeriod = findViewById(R.id.buttonNextPeriod);

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

        // Set up period navigation buttons
        buttonPrevPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                periodOffset--;
                updateSummaryTitle();
                loadBudgetSettings(); // Load budget for new period
                loadTransactionsForPeriod();
            }
        });

        buttonNextPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (periodOffset < 0) {
                    periodOffset++;
                    updateSummaryTitle();
                    loadBudgetSettings(); // Load budget for new period
                    loadTransactionsForPeriod();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Already at current period", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePeriodNavigationVisibility() {
        View layoutPeriodNavigation = findViewById(R.id.layoutPeriodNavigation);

        if (isViewingHistory) {
            // Show navigation container when viewing history
            layoutPeriodNavigation.setVisibility(View.VISIBLE);

            // Show navigation buttons when viewing history
            buttonPrevPeriod.setVisibility(View.VISIBLE);
            buttonNextPeriod.setVisibility(View.VISIBLE);

            // Hide add transaction buttons when viewing history
            buttonAddExpense.setVisibility(View.GONE);
            buttonAddIncome.setVisibility(View.GONE);
        } else {
            // Hide navigation container when viewing current period
            layoutPeriodNavigation.setVisibility(View.GONE);

            // Hide navigation buttons when viewing current period
            buttonPrevPeriod.setVisibility(View.GONE);
            buttonNextPeriod.setVisibility(View.GONE);

            // Show add transaction buttons when viewing current period
            buttonAddExpense.setVisibility(View.VISIBLE);
            buttonAddIncome.setVisibility(View.VISIBLE);
        }
    }

    private void loadBudgetSettings() {
        // Get budget from database for current period (based on offset)
        Budget budget = dbHelper.getBudgetByOffset(periodOffset);

        if (budget != null) {
            // Use the budget from database
            budgetAmount = budget.getAmount();
        } else {
            // Default value if no budget is set
            budgetAmount = 1000.00;
        }

        // Update the budget display
        textViewBudgetTotal.setText(String.format("$%.2f monthly budget", budgetAmount));

        // Reset 90% alert flag when new period is viewed (only if periodOffset == 0)
        if (periodOffset == 0) {
            SharedPreferences prefs = getSharedPreferences("BudgetPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("budget_alert_shown", false).apply();
        }
    }

    private void updateSummaryData() {
        // Get monthly data
        double totalIncome = dbHelper.getMonthIncome(periodOffset);
        double totalExpenses = dbHelper.getMonthExpenses(periodOffset);

        // Update income, expenses, and balance text views
        textViewIncome.setText(String.format("$%.2f", totalIncome));
        textViewExpenses.setText(String.format("$%.2f", totalExpenses));
        double balance = totalIncome - totalExpenses;
        textViewBalance.setText(String.format("$%.2f", balance));

        // Update budget progress bar
        int budgetPercentage = budgetAmount > 0 ? (int) ((totalExpenses / budgetAmount) * 100) : 0;
        progressBarBudget.setProgress(Math.min(budgetPercentage, 100));

        // Update spent text (budgetTotal is updated in loadBudgetSettings)
        textViewSpent.setText(String.format("$%.2f spent", totalExpenses));

        // Trigger budget notification if over 90%
        if (budgetPercentage >= 90 && periodOffset == 0) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean alertShown = prefs.getBoolean(PREF_BUDGET_ALERT_SHOWN, false);

            if (!alertShown) {
                BudgetNotificationHelper.showBudgetWarningNotification(this, totalExpenses, budgetAmount);
                prefs.edit().putBoolean(PREF_BUDGET_ALERT_SHOWN, true).apply();
            }
        }

    }

    private void loadTransactionsForPeriod() {
        // Update summary data with the current period offset
        updateSummaryData();

        // Get monthly transactions
        List<Transaction> transactions = dbHelper.getMonthTransactions(periodOffset);

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
        // Reload budget settings
        loadBudgetSettings();

        // Update UI based on current view state
        updatePeriodNavigationVisibility();
        updateSummaryTitle();

        // Refresh data from the database
        loadTransactionsForPeriod();
    }

    private void updateSummaryTitle() {
        TextView summaryTitle = findViewById(R.id.summaryTitleTextView);
        if (summaryTitle != null) {
            // Get current month name and year for display
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, periodOffset);
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            String currentPeriod = monthYearFormat.format(calendar.getTime());

            if (periodOffset == 0) {
                summaryTitle.setText("Current Month Summary (" + currentPeriod + ")");
            } else {
                summaryTitle.setText(Math.abs(periodOffset) + " Month" + (Math.abs(periodOffset) > 1 ? "s" : "") +
                        " Ago Summary (" + currentPeriod + ")");
            }
        }
    }
}