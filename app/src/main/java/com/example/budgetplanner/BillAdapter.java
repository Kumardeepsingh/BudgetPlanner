package com.example.budgetplanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import java.util.List;

public class BillAdapter extends ArrayAdapter<Bill> {

    private final Context context;
    private List<Bill> bills;

    public BillAdapter(Context context, List<Bill> bills) {
        super(context, R.layout.item_bill, bills);
        this.context = context;
        this.bills = bills;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        }

        Bill bill = bills.get(position);

        TextView textViewBillName = convertView.findViewById(R.id.textViewBillName);
        TextView textViewBillAmount = convertView.findViewById(R.id.textViewBillAmount);
        TextView textViewBillDate = convertView.findViewById(R.id.textViewBillDate);

        textViewBillName.setText(bill.getBillName());
        textViewBillAmount.setText("$" + bill.getAmount());
        textViewBillDate.setText("Due: " + bill.getDueDate());

        return convertView;
    }

    // Ensure Adapter Refreshes When Called
    public void updateData(List<Bill> newBills) {
        this.bills = newBills;
        notifyDataSetChanged();
    }
}