package com.example.budgetplanner;

public class Budget {
    private long id;
    private String yearMonth; // Format: "YYYY-MM"
    private double amount;

    public Budget() {
    }

    public Budget(long id, String yearMonth, double amount) {
        this.id = id;
        this.yearMonth = yearMonth;
        this.amount = amount;
    }

    public Budget(String yearMonth, double amount) {
        this.yearMonth = yearMonth;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}