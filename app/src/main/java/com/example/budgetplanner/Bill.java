package com.example.budgetplanner;

public class Bill {
    private int billID;
    private String billName;
    private double amount;
    private String dueDate;
    private String description;
    private boolean isPaid;

    // Constructor
    public Bill(int billID, String billName, double amount, String dueDate, String description, boolean isPaid) {
        this.billID = billID;
        this.billName = billName;
        this.amount = amount;
        this.dueDate = dueDate;
        this.description = description;
        this.isPaid = isPaid;
    }

    // Getters
    public int getBillID() {
        return billID;
    }

    public String getBillName() {
        return billName;
    }

    public double getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaid() {
        return isPaid;
    }

    // Setters
    public void setBillID(int billID) {
        this.billID = billID;
    }

    public void setBillName(String billName) {
        this.billName = billName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}