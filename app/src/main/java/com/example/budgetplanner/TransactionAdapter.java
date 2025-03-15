package com.example.budgetplanner;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends BaseAdapter {

    private List<Transaction> transactionList;
    private Context context;
    private SimpleDateFormat dateFormat;
    private LayoutInflater inflater;

    // Constructor
    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return transactionList.size();
    }

    @Override
    public Transaction getItem(int position) {
        return transactionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_transaction, parent, false);

            holder = new ViewHolder();
            holder.textViewCategory = convertView.findViewById(R.id.textViewTransactionCategory);
            holder.textViewDescription = convertView.findViewById(R.id.textViewTransactionDescription);
            holder.textViewDate = convertView.findViewById(R.id.textViewTransactionDate);
            holder.textViewAmount = convertView.findViewById(R.id.textViewTransactionAmount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the transaction for this position
        Transaction transaction = getItem(position);

        // Set category
        holder.textViewCategory.setText(transaction.getCategory());

        // Set description (if empty, show "No description")
        String description = transaction.getDescription();
        holder.textViewDescription.setText(description.isEmpty() ? "No description" : description);

        // Format and set date
        holder.textViewDate.setText(dateFormat.format(transaction.getDate()));

        // Format and set amount with appropriate color
        String amountText;
        if (transaction.isIncome()) {
            amountText = String.format("+$%.2f", transaction.getAmount());
            holder.textViewAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            amountText = String.format("-$%.2f", transaction.getAmount());
            holder.textViewAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }
        holder.textViewAmount.setText(amountText);

        return convertView;
    }

    // Method to update the data
    public void updateData(List<Transaction> newTransactions) {
        this.transactionList = newTransactions;
        notifyDataSetChanged();
    }

    // ViewHolder pattern for better ListView performance
    private static class ViewHolder {
        TextView textViewCategory;
        TextView textViewDescription;
        TextView textViewDate;
        TextView textViewAmount;
    }
}