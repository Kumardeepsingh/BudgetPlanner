package com.example.budgetplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends BaseAdapter {

    private final Context context;
    private List<Bill> billList;
    private LayoutInflater inflater;
    private SimpleDateFormat dateFormat;

    public BillAdapter(Context context, List<Bill> billList) {

        this.context = context;
        this.billList = billList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return billList.size();
    }

    @Override
    public Bill getItem(int position) {
        return billList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_bill, parent, false);

            holder = new ViewHolder();

            holder.textViewBillName = convertView.findViewById(R.id.textViewBillName);
            holder.textViewBillAmount = convertView.findViewById(R.id.textViewBillAmount);
            holder.textViewBillDate = convertView.findViewById(R.id.textViewBillDate);
            // holder.textViewAmount = convertView.findViewById(R.id.textViewTransactionAmount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the transaction for this position
        Bill bill = getItem(position);

        holder.textViewBillName.setText(bill.getBillName());
        //holder.textViewBillAmount.setText("$" + bill.getAmount());

        try {
            Date parsedDate = new SimpleDateFormat("yyyy-M-d", Locale.getDefault()).parse(bill.getDueDate());
            String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(parsedDate);
            holder.textViewBillDate.setText("Due: " + formattedDate);
        } catch (ParseException e) {
            holder.textViewBillDate.setText("Due: " + bill.getDueDate()); // fallback
        }

        // Format and set amount with appropriate color
        String amountText;

        amountText = String.format("$%.2f", bill.getAmount());
        holder.textViewBillAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        holder.textViewBillAmount.setText(amountText);

        return convertView;
    }

    // Method to update the data
    public void updateData(List<Bill> newBills) {
        this.billList = newBills;
        notifyDataSetChanged();
    }

    // ViewHolder pattern for better ListView performance
    private static class ViewHolder {
        TextView textViewBillName;
        TextView textViewBillAmount;
        TextView textViewBillDate;
    }
}