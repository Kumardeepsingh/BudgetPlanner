package com.example.budgetplanner;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddBillActivity extends AppCompatActivity {


    private EditText editTextBillName, editTextBillAmount, editTextBillDate, editTextBillDescription;
    private Button buttonSaveBill;
    private DbHandler dbHandler;
    private Calendar calendar;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_bill);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI Components
        dbHandler = new DbHandler(this);
        editTextBillName = findViewById(R.id.editTextBillName);
        editTextBillAmount = findViewById(R.id.editTextBillAmount);
        editTextBillDate = findViewById(R.id.editTextBillDate);
        editTextBillDescription = findViewById(R.id.editTextBillDescription);
        buttonSaveBill = findViewById(R.id.buttonSaveBill);

        // Open Date Picker when clicking on Bill Date field
        editTextBillDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Handle Save Bill Button Click
        buttonSaveBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveBill();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_budget_settings) {
            Intent intent = new Intent(AddBillActivity.this, BudgetSettingActivity.class);
            startActivity(intent);
            return true;
        }else if (itemId == R.id.menu_navigation_bills) {
            Intent intent = new Intent(AddBillActivity.this, BillManagementActivity.class);
            startActivity(intent);
            return true;
        }
        else if (itemId == R.id.menu_home) {
            Intent intent = new Intent(AddBillActivity.this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Toggle menu items based on current view
        menu.findItem(R.id.menu_view_history).setVisible(false);
        menu.findItem(R.id.menu_current_period).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    private void showDatePicker() {
        calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            editTextBillDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void saveBill() throws ParseException {
        String billName = editTextBillName.getText().toString().trim();
        String amountStr = editTextBillAmount.getText().toString().trim();
        String dueDate = editTextBillDate.getText().toString().trim();
        String description = editTextBillDescription.getText().toString().trim();

        if (billName.isEmpty() || amountStr.isEmpty() || dueDate.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        Date date = dateFormat.parse(dueDate);
        long billID = dbHandler.addBill(billName, amount,date , description);

        if (billID != -1) {
            scheduleBillNotification(billName, dueDate);
            Toast.makeText(this, "Bill added successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AddBillActivity.this, BillManagementActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Optional: Prevents stack duplicates
            startActivity(intent);

        } else {
            Toast.makeText(this, "Failed to add bill", Toast.LENGTH_SHORT).show();
        }

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }
    }

    private void scheduleBillNotification(String billName, String dueDateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d", Locale.getDefault());
            Date dueDate = sdf.parse(dueDateString);

            if (dueDate == null) {
                Log.e("AddBillActivity", "Failed to parse due date: " + dueDateString);
                return;
            }

            // Set up notification for 2 days before the due date at 10 AM
            Calendar notificationCalendar = Calendar.getInstance();
            notificationCalendar.setTime(dueDate);
            notificationCalendar.add(Calendar.DAY_OF_YEAR, -2); // 2 days before
            notificationCalendar.set(Calendar.HOUR_OF_DAY, 10);
            notificationCalendar.set(Calendar.MINUTE, 0);
            notificationCalendar.set(Calendar.SECOND, 0);

            long triggerTime = notificationCalendar.getTimeInMillis();
            long currentTime = System.currentTimeMillis();

            // Only schedule if trigger time is in the future
            if (triggerTime <= currentTime) {
                Log.d("AddBillActivity", "Not scheduling notification - date is in the past");
                return;
            }

            int requestCode = (billName + dueDateString).hashCode();
            Intent intent = new Intent(this, BillReminderReceiver.class);
            intent.putExtra("billName", billName);
            intent.putExtra("dueDate", dueDateString);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            // Check for permission to schedule exact alarms on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    Log.d("AddBillActivity", "Exact alarm scheduled for " + billName);
                } else {
                    // Fall back to inexact alarm
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    Log.d("AddBillActivity", "Inexact alarm scheduled (permission not granted)");

                    // Show alert dialog
                    showAlarmPermissionDialog();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d("AddBillActivity", "Exact alarm scheduled for " + billName);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                Log.d("AddBillActivity", "Exact alarm scheduled for " + billName);
            }

        } catch (Exception e) {
            Log.e("AddBillActivity", "Error scheduling notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlarmPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Permission Required")
                .setMessage("For precise bill reminders, please enable the \"Schedule Exact Alarms\" permission in your device settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    // Direct the user to the exact alarm permission settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Later", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Bill added but reminders may not be precise", Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            // Check if notification permission was granted
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Log.d("AddBillActivity", "Notification permission granted");
            } else {
                Log.d("AddBillActivity", "Notification permission denied");
                Toast.makeText(this, "Notifications won't work without permission", Toast.LENGTH_LONG).show();
            }
        }
    }

}