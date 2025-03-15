package com.example.budgetplanner;

import java.util.Date;

public class Transaction {
    private long id;
    private String type; // "income" or "expense"
    private double amount;
    private String category;
    private Date date;
    private String description;

    // Constructor
    public Transaction(long id, String type, double amount, String category, Date date, String description) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Constructor without ID (for new transactions)
    public Transaction(String type, double amount, String category, Date date, String description) {
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Helper method to determine if this is an expense
    public boolean isExpense() {
        return "expense".equalsIgnoreCase(type);
    }

    // Helper method to determine if this is income
    public boolean isIncome() {
        return "income".equalsIgnoreCase(type);
    }
}
