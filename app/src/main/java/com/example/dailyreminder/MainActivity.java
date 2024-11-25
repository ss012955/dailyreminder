package com.example.dailyreminder;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Reminder> reminders;
    private ReminderAdapter adapter;
    private ActivityResultLauncher<Intent> addReminderLauncher;
    private ActivityResultLauncher<Intent> editReminderLauncher;
    private static final String CHANNEL_ID = "daily_reminders";
    private ReminderDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new ReminderDatabaseHelper(this);
        checkPermissions();
        createNotificationChannel();
        initializeUI();
        setupActivityResultLaunchers();

        // Register Broadcast Receiver
        registerReminderBroadcastReceiver();
    }

    /**
     * Checks and requests required permissions for notifications and alarms.
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    /**
     * Initializes the RecyclerView and sets up the adapter.
     */
    private void initializeUI() {
        RecyclerView rvReminders = findViewById(R.id.rvReminders);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        ReminderDatabaseHelper dbHelper = new ReminderDatabaseHelper(this);
        reminders = new ArrayList<>();
        // Load reminders from the database
        Cursor cursor = dbHelper.getAllReminders();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_DESCRIPTION));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(ReminderDatabaseHelper.COLUMN_TIME));

            reminders.add(new Reminder(id, title, description, time));
        }
        cursor.close();

        adapter = new ReminderAdapter(reminders, new ReminderAdapter.OnReminderClickListener() {
            @Override
            public void onReminderClick(Reminder reminder) {
                Log.d("MainActivity", "Clicked reminder with ID: " + reminder.getId());
                Toast.makeText(MainActivity.this, "Reminder: " + reminder.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Reminder reminder, int position) {
                Intent intent = new Intent(MainActivity.this, activity_add_reminder.class);
                intent.putExtra("reminder", reminder);
                intent.putExtra("position", position);
                editReminderLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Reminder reminder, int position) {
                Log.d("MainActivity", "Deleting reminder with ID: " + reminder.getId());
                removeReminderByPosition(position);
                cancelScheduledNotification(reminder);
            }
        });
        rvReminders.setAdapter(adapter);

        FloatingActionButton fabAddReminder = findViewById(R.id.fabAddReminder);
        fabAddReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, activity_add_reminder.class);
            addReminderLauncher.launch(intent);

        });
    }

    /**
     * Sets up ActivityResultLaunchers for adding and editing reminders.
     */
    private void setupActivityResultLaunchers() {
        addReminderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Reminder newReminder = (Reminder) result.getData().getSerializableExtra("updatedReminder");
                        // Add to database
                        long newId = dbHelper.addReminder(
                                newReminder.getTitle(),
                                newReminder.getDescription(),
                                newReminder.getTime()
                        );
                        newReminder.setId((int) newId);
                        reminders.add(newReminder);
                        adapter.notifyItemInserted(reminders.size() - 1);
                        scheduleNotification(newReminder);
                    }
                }
        );

        editReminderLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Reminder updatedReminder = (Reminder) result.getData().getSerializableExtra("updatedReminder");
                        int position = result.getData().getIntExtra("position", -1);
                        if (position >= 0 && position < reminders.size()) {
                            dbHelper.updateReminder(updatedReminder.getId(), updatedReminder.getTitle(), updatedReminder.getDescription(), updatedReminder.getTime());
                            cancelScheduledNotification(reminders.get(position));
                            reminders.set(position, updatedReminder);
                            adapter.notifyItemChanged(position);
                            scheduleNotification(updatedReminder);
                        }
                    }
                }
        );
    }
    private void scheduleNotification(Reminder reminder) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            intent.putExtra("title", reminder.getTitle());
            intent.putExtra("description", reminder.getDescription());
            intent.putExtra("reminderId", reminder.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    reminder.getId(), // Use the unique Reminder ID
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            String[] timeParts = reminder.getTime().split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1].replaceAll("[^0-9]", ""));

            // Adjust for AM/PM
            if (reminder.getTime().contains("PM") && hour != 12) {
                hour += 12;
            } else if (reminder.getTime().contains("AM") && hour == 12) {
                hour = 0;
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (alarmManager != null) {
                long triggerTime = calendar.getTimeInMillis();
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error scheduling notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Registers the BroadcastReceiver for reminder actions.
     */
    private void registerReminderBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.dailyreminder.ACTION_DELETE");
        filter.addAction("com.example.dailyreminder.ACTION_DONE");

        LocalBroadcastManager.getInstance(this).registerReceiver(reminderBroadcastReceiver, filter);
    }

    /**
     * Handles broadcasts for deleting or completing reminders.
     */
    private final BroadcastReceiver reminderBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int reminderId = intent.getIntExtra("reminderId", -1);
            Log.d("MainActivity", "Received action: " + action + ", Reminder ID: " + reminderId);
            if ("com.example.dailyreminder.ACTION_DELETE".equals(action)) {
                Log.d("MainActivity", "Attempting to delete reminder with ID: " + reminderId);
                removeReminderById(reminderId);
            } else if ("com.example.dailyreminder.ACTION_DONE".equals(action)) {
                Log.d("MainActivity", "Attempting to mark as completed reminder with ID: " + reminderId);
                markReminderAsCompleted(reminderId);
            }
        }
    };

    /**
     * Removes a reminder by its position.
     */
    private void removeReminderByPosition(int position) {
        if (position >= 0 && position < reminders.size()) {
            dbHelper.deleteReminder(reminders.get(position).getId());
            reminders.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, reminders.size());
            cancelNotification(reminders.get(position).getId());
        }
    }

    /**
     * Removes a reminder by its ID.
     */
    private void removeReminderById(int reminderId) {
        for (int i = 0; i < reminders.size(); i++) {
            Log.d("MainActivity", "Checking reminder at position " + i + " with ID: " + reminders.get(i).getId());
            if (reminders.get(i).getId() == reminderId) {
                dbHelper.deleteReminder(reminderId);
                reminders.remove(i);
                adapter.notifyItemRemoved(i);
                cancelNotification(reminderId);
                Log.d("MainActivity", "Removed reminder with ID: " + reminderId);
                return;
            }
        }
        Log.d("MainActivity", "Reminder with ID " + reminderId + " not found.");
    }

    /**
     * Marks a reminder as completed.
     */
    private void markReminderAsCompleted(int reminderId) {
        for (Reminder reminder : reminders) {
            Log.d("MainActivity", "Checking reminder with ID: " + reminder.getId());
            if (reminder.getId() == reminderId) {
                reminder.setCompleted(true);
                adapter.notifyDataSetChanged();
                cancelNotification(reminderId);
                Log.d("MainActivity", "Marked reminder with ID " + reminderId + " as completed.");
                return;

            }
        }
        Log.d("MainActivity", "Reminder with ID " + reminderId + " not found.");
    }

    /**
     * Creates a notification channel for reminders.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Channel for daily reminder notifications");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Cancels a scheduled notification for a reminder.
     */
    private void cancelScheduledNotification(Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Create the same Intent used for scheduling
        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("description", reminder.getDescription());
        intent.putExtra("reminderId", reminder.getId());

        // Use the same unique ID for the PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                reminder.getId(), // Must match the ID used when scheduling
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent); // Cancel the scheduled alarm
        }
    }

    private void cancelNotification(int reminderId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(reminderId); // Use reminder ID as the notification ID
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(reminderBroadcastReceiver);
    }
}