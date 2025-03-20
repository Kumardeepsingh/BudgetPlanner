package com.example.budgetplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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

    private void loadBills() {
        List<Bill> bills = dbHandler.getAllBills();

        if (bills.isEmpty()) {
            textViewNoBills.setVisibility(View.VISIBLE);
            listViewBills.setVisibility(View.GONE);
        } else {
            textViewNoBills.setVisibility(View.GONE);
            listViewBills.setVisibility(View.VISIBLE);

            billAdapter = new BillAdapter(this, bills);
            listViewBills.setAdapter(billAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBills(); // Refresh list when returning to this screen
    }
}