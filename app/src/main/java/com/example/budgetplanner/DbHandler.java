package com.example.budgetplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;

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

    public DbHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creating required tables
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // On upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

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



// Add these new methods to your DbHandler class

    /**
     * Gets transactions for the current period (week or month)
     */
    public List<Transaction> getPeriodTransactions(String budgetPeriod, int offset) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates based on budget period and offset
        Date[] periodDates = getPeriodDates(budgetPeriod, offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for transactions in specified period
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

    private Date[] getPeriodDates(String budgetPeriod, int offset) {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();

        // Set to current date and apply offset
        if (budgetPeriod.equals(BudgetSettingActivity.PERIOD_WEEKLY)) {
            // Weekly period
            calStart.add(Calendar.WEEK_OF_YEAR, offset);
            calEnd.add(Calendar.WEEK_OF_YEAR, offset);

            // Set to first day of week
            calStart.set(Calendar.DAY_OF_WEEK, calStart.getFirstDayOfWeek());

            // Set to last day of week
            calEnd.set(Calendar.DAY_OF_WEEK, calStart.getFirstDayOfWeek() + 6);
        } else {
            // Monthly period
            calStart.add(Calendar.MONTH, offset);
            calEnd.add(Calendar.MONTH, offset);

            // Set to first day of month
            calStart.set(Calendar.DAY_OF_MONTH, 1);

            // Set to last day of month
            calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        }

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
     * Get total income for the current period
     */
    public double getPeriodIncome(String budgetPeriod, int offset) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates for the period
        Date[] periodDates = getPeriodDates(budgetPeriod, offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for sum of income in specified period
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
     * Get total expenses for the current period
     */
    public double getPeriodExpenses(String budgetPeriod, int offset) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end dates for the period
        Date[] periodDates = getPeriodDates(budgetPeriod, offset);
        Date startDate = periodDates[0];
        Date endDate = periodDates[1];

        String startDateStr = dateFormat.format(startDate);
        String endDateStr = dateFormat.format(endDate);

        // Query for sum of expenses in specified period
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
    /*
     * Deleting a transaction
     */
    public void deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSACTIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }
}