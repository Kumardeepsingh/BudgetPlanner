package com.example.budgetplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

public class DbHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "BudgetPlannerDB";

    // Table Names
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_BUDGETS = "budgets";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";

    // Budget table columns
    private static final String KEY_YEAR_MONTH = "year_month";

    // Date format for storing dates
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

    // Table Create Statements
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TYPE + " TEXT NOT NULL,"
            + KEY_AMOUNT + " REAL NOT NULL,"
            + KEY_CATEGORY + " TEXT NOT NULL,"
            + KEY_DATE + " TEXT NOT NULL,"
            + KEY_DESCRIPTION + " TEXT"
            + ")";

    private static final String CREATE_TABLE_BUDGETS = "CREATE TABLE " + TABLE_BUDGETS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_YEAR_MONTH + " TEXT UNIQUE NOT NULL,"
            + KEY_AMOUNT + " REAL NOT NULL"
            + ")";

    private static final String CREATE_TABLE_BILLS = "CREATE TABLE IF NOT EXISTS bills (" +
            "billID INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "billName TEXT NOT NULL, " +
            "amount REAL NOT NULL, " +
            "dueDate TEXT NOT NULL, " +
            "description TEXT, " +
            "isPaid INTEGER DEFAULT 0)"; // 0 = Unpaid, 1 = Paid


    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_BILLS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS bills");



        // Create new tables
        onCreate(db);
    }

    /*
     * Adding a new transaction
     */
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, transaction.getType());
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_CATEGORY, transaction.getCategory());
        values.put(KEY_DATE, dateFormat.format(transaction.getDate()));
        values.put(KEY_DESCRIPTION, transaction.getDescription());

        // Insert row
        long id = db.insert(TABLE_TRANSACTIONS, null, values);

        // Close database connection
        db.close();

        return id;
    }

    /**
     * Gets transactions for the current month
     */
    public List<Transaction> getMonthTransactions(int offset) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates based on month and offset
        Date[] periodDates = getMonthDates(offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for transactions in specified month
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE date(" + KEY_DATE + ") >= date('" + startDateStr + "')" +
                " AND date(" + KEY_DATE + ") <= date('" + endDateStr + "')" +
                " ORDER BY " + KEY_DATE + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to list
        if (cursor.moveToFirst()) {
            do {
                try {
                    Transaction transaction = new Transaction(
                            cursor.getLong(0),
                            cursor.getString(1),
                            cursor.getDouble(2),
                            cursor.getString(3),
                            dateFormat.parse(cursor.getString(4)),
                            cursor.getString(5)
                    );

                    // Add transaction to list
                    transactions.add(transaction);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }

        // Close the cursor
        cursor.close();

        // Return transactions list
        return transactions;
    }

    private Date[] getMonthDates(int offset) {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        // Set to current date and apply offset
        calStart.add(Calendar.MONTH, offset);
        calEnd.add(Calendar.MONTH, offset);

        // Set to first day of month
        calStart.set(Calendar.DAY_OF_MONTH, 1);

        // Set to last day of month
        calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Reset time to beginning of day for start date
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        // Reset time to end of day for end date
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);

        return new Date[] { calStart.getTime(), calEnd.getTime() };
    }

    /**
     * Get total income for the current month
     */
    public double getMonthIncome(int offset) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates for the month
        Date[] periodDates = getMonthDates(offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for sum of income in specified month
        String query = "SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + KEY_TYPE + "='income'" +
                " AND date(" + KEY_DATE + ") >= date('" + startDateStr + "')" +
                " AND date(" + KEY_DATE + ") <= date('" + endDateStr + "')";

        Cursor cursor = db.rawQuery(query, null);

        double totalIncome = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalIncome = cursor.getDouble(0);
        }
        cursor.close();

        return totalIncome;
    }

    /**
     * Get total expenses for the current month
     */
    public double getMonthExpenses(int offset) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates for the month
        Date[] periodDates = getMonthDates(offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for sum of expenses in specified month
        String query = "SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + KEY_TYPE + "='expense'" +
                " AND date(" + KEY_DATE + ") >= date('" + startDateStr + "')" +
                " AND date(" + KEY_DATE + ") <= date('" + endDateStr + "')";

        Cursor cursor = db.rawQuery(query, null);

        double totalExpenses = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            totalExpenses = cursor.getDouble(0);
        }
        cursor.close();

        return totalExpenses;
    }

    /**
     * Delete a transaction
     */
    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }

    /**
     * Get current year-month string
     */
    public String getCurrentYearMonth() {
        return yearMonthFormat.format(new Date());
    }

    /**
     * Get year-month string for a specific offset
     */
    public String getYearMonthByOffset(int offset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, offset);
        return yearMonthFormat.format(calendar.getTime());
    }

    /**
     * Get budget for a specific month offset
     */
    public Budget getBudgetByOffset(int offset) {
        // Get year-month string for the specified offset
        String yearMonth = getYearMonthByOffset(offset);

        // Get budget for this year-month
        return getBudget(yearMonth);
    }

    /**
     * Set budget for a specific year-month
     */
    public long setBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_YEAR_MONTH, budget.getYearMonth());
        values.put(KEY_AMOUNT, budget.getAmount());

        // Check if this year-month already has a budget
        Budget existingBudget = getBudget(budget.getYearMonth());
        long id;

        if (existingBudget != null) {
            // Update existing budget
            id = existingBudget.getId();
            db.update(TABLE_BUDGETS, values, KEY_ID + " = ?",
                    new String[] { String.valueOf(id) });
        } else {
            // Insert new budget
            id = db.insert(TABLE_BUDGETS, null, values);
        }

        db.close();
        return id;
    }

    /**
     * Get budget for a specific year-month
     */
    public Budget getBudget(String yearMonth) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS +
                " WHERE " + KEY_YEAR_MONTH + " = '" + yearMonth + "'";

        Cursor cursor = db.rawQuery(selectQuery, null);
        Budget budget = null;

        if (cursor.moveToFirst()) {
            budget = new Budget(
                    cursor.getLong(0),  // id
                    cursor.getString(1), // year_month
                    cursor.getDouble(2)  // amount
            );
        }

        cursor.close();
        return budget;
    }

    /**
     * Get all budgets
     */
    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS +
                " ORDER BY " + KEY_YEAR_MONTH + " DESC";

        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget(
                        cursor.getLong(0),  // id
                        cursor.getString(1), // year_month
                        cursor.getDouble(2)  // amount
                );

                budgets.add(budget);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return budgets;
    }

    public long addBill(String billName, double amount, String dueDate, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("billName", billName);
        values.put("amount", amount);
        values.put("dueDate", dueDate);
        values.put("description", description);
        values.put("isPaid", 0); // Default: unpaid

        long id = db.insert("bills", null, values);

        if (id == -1) {
            Log.e("DBDebug", "Failed to insert bill into database.");
        } else {
            Log.d("DBDebug", "Bill added successfully with ID: " + id);
        }

        db.close();
        return id;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM bills ORDER BY dueDate ASC";
        Cursor cursor = db.rawQuery(query, null);
        Log.d("DBDebug", "Fetching bills from database...");

        if (cursor.moveToFirst()) {
            do {
                int billID = cursor.getInt(cursor.getColumnIndexOrThrow("billID"));
                String billName = cursor.getString(cursor.getColumnIndexOrThrow("billName"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow("dueDate"));
                String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                boolean isPaid = cursor.getInt(cursor.getColumnIndexOrThrow("isPaid")) == 1; // Correct Boolean Parsing

                Log.d("DBDebug", "Fetched Bill: ID=" + billID + ", Name=" + billName);
                // Ensure `Bill` constructor matches expected types
                bills.add(new Bill(billID, billName, amount, dueDate, description, isPaid));
            } while (cursor.moveToNext());
        } else {
            Log.d("DBDebug", "No bills found in database.");
        }

        cursor.close();
        db.close();
        return bills;
    }

    public void markBillAsPaid(int billID) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isPaid", 1);
        db.update("bills", values, "billID = ?", new String[]{String.valueOf(billID)});
        db.close();
    }

    public void deleteBill(int billID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("bills", "billID = ?", new String[]{String.valueOf(billID)});
        db.close();
    }
}