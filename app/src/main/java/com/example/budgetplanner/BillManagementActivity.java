package com.example.budgetplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class BillManagementActivity extends AppCompatActivity {

    Button buttonAddBill;
    private ListView listViewBills;
    private TextView textViewNoBills;
    private DbHandler dbHandler;
    private BillAdapter billAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonAddBill = findViewById(R.id.buttonAddBill);
        listViewBills = findViewById(R.id.listViewBills);
        textViewNoBills = findViewById(R.id.textViewNoBills);
        dbHandler = new DbHandler(this);

        // Handle "Add Bill" button click
        buttonAddBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BillManagementActivity.this, AddBillActivity.class);
                startActivity(intent);
            }
        });
        // Load Bills
        loadBills();
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
            Intent intent = new Intent(BillManagementActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(BillManagementActivity.this, MainActivity.class);
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
        menu.findItem(R.id.menu_navigation_bills).setVisible(false);


        return super.onPrepareOptionsMenu(menu);
    }

    private void loadBills() {
        List<Bill> bills = dbHandler.getAllBills();

        if (bills.isEmpty()) {
            textViewNoBills.setVisibility(View.VISIBLE);
            listViewBills.setVisibility(View.GONE);
        } else {
            textViewNoBills.setVisibility(View.GONE);
            listViewBills.setVisibility(View.VISIBLE);

            // Initialize adapter if needed
            if (billAdapter == null) {
                billAdapter = new BillAdapter(this, bills);
                listViewBills.setAdapter(billAdapter);
            } else {
                billAdapter.updateData(bills);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills(); // Refresh list when returning to this screen
    }
}