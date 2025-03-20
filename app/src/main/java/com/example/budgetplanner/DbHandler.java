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
import com.example.budgetplanner.Bill;


public class DbHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "BudgetPlannerDB";

    // Table Names
    private static final String TABLE_TRANSACTIONS = "transactions";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_TYPE = "type";
    private static final String KEY_AMOUNT = "amount";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_DATE = "date";
    private static final String KEY_DESCRIPTION = "description";

    // Date format for storing dates
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    // Table Create Statements
    private static final String CREATE_TABLE_TRANSACTIONS = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_TYPE + " TEXT NOT NULL,"
            + KEY_AMOUNT + " REAL NOT NULL,"
            + KEY_CATEGORY + " TEXT NOT NULL,"
            + KEY_DATE + " TEXT NOT NULL,"
            + KEY_DESCRIPTION + " TEXT"
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
        db.execSQL(CREATE_TABLE_BILLS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

        // Create new tables
        onCreate(db);
    }

    // ========== Transaction CRUD operations ==========

    /*
     * Adding a new transaction
     */
    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        SimpleDateFormat customFormat = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
        String formattedDate = customFormat.format(transaction.getDate());

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, transaction.getType());
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_CATEGORY, transaction.getCategory());
        values.put(KEY_DATE, formattedDate);  // Ensure date is stored correctly
        values.put(KEY_DESCRIPTION, transaction.getDescription());

        Log.d("DBDebug", "Inserting transaction: Type=" + transaction.getType() +
                ", Amount=" + transaction.getAmount() +
                ", Date=" + transaction.getDate());

        long id = db.insert(TABLE_TRANSACTIONS, null, values);

        if (id == -1) {
            Log.e("DBDebug", "Transaction insertion failed.");
        }

        db.close();
        return id;
    }



    /*
     * Getting a single transaction
     */
    public Transaction getTransaction(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_TRANSACTIONS,
                new String[] { KEY_ID, KEY_TYPE, KEY_AMOUNT, KEY_CATEGORY, KEY_DATE, KEY_DESCRIPTION },
                KEY_ID + "=?", new String[] { String.valueOf(id) },
                null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;

        Transaction transaction = null;

        try {
            transaction = new Transaction(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getDouble(2),
                    cursor.getString(3),
                    dateFormat.parse(cursor.getString(4)),
                    cursor.getString(5)
            );
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Close the cursor
        cursor.close();

        // Return transaction
        return transaction;
    }

    /*
     * Getting all transactions
     */
    public List<Transaction> getAllTransactions(String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();

        startDate = startDate.trim(); // Ensure no extra spaces
        endDate = endDate.trim();

        Log.d("DBDebug", "Fetching transactions from " + startDate + " to " + endDate);

        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_TRANSACTIONS +
                " WHERE " + KEY_DATE + " BETWEEN ? AND ? " +
                " ORDER BY " + KEY_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{startDate, endDate});

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

    /*
     * Getting transaction count
     */
    public int getTransactionsCount() {
        String countQuery = "SELECT * FROM " + TABLE_TRANSACTIONS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        return count;
    }



    /*
     * Get total income
     */
    public double getTotalIncome() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + KEY_TYPE + "='income'", null);

        double totalIncome = 0;
        if (cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(0);
        }
        cursor.close();

        return totalIncome;
    }

    public double getTotalIncomeForPeriod(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalIncome = 0;

        String query = "SELECT SUM(amount) FROM transactions WHERE type='income' AND date BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            totalIncome = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return totalIncome;
    }


    public double getTotalExpensesForPeriod(String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        double totalExpenses = 0;

        String query = "SELECT SUM(amount) FROM transactions WHERE type='expense' AND date BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(query, new String[]{startDate, endDate});

        if (cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0);
        }

        cursor.close();
        db.close();
        return totalExpenses;
    }

    /*
     * Get total expenses
     */
    public double getTotalExpenses() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + KEY_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + KEY_TYPE + "='expense'", null);

        double totalExpenses = 0;
        if (cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(0);
        }
        cursor.close();

        return totalExpenses;
    }

    /*
     * Updating a transaction
     */
    public int updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_TYPE, transaction.getType());
        values.put(KEY_AMOUNT, transaction.getAmount());
        values.put(KEY_CATEGORY, transaction.getCategory());
        values.put(KEY_DATE, dateFormat.format(transaction.getDate()));
        values.put(KEY_DESCRIPTION, transaction.getDescription());

        // Updating row
        return db.update(TABLE_TRANSACTIONS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(transaction.getId()) });
    }

    /*
     * Deleting a transaction
     */
    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
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