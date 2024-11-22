package com.example.dailyreminder;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import android.app.PendingIntent;

import com.example.dailyreminder.ReminderBroadcastReceiver;

public class ReminderNotificationHelper {
    public static final String CHANNEL_ID = "daily_reminders"; // Make it public
    private static final String CHANNEL_NAME = "Daily Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for daily reminders";


    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription(CHANNEL_DESCRIPTION);
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Displays a notification with a snooze option.
     */
    public static void showNotificationWithSnooze(Context context, String title, String description, int reminderId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Snooze Action
        Intent snoozeIntent = new Intent(context, ReminderBroadcastReceiver.class);
        snoozeIntent.setAction("com.example.dailyreminder.ACTION_SNOOZE");
        snoozeIntent.putExtra("title", title);
        snoozeIntent.putExtra("description", description);
        snoozeIntent.putExtra("reminderId", reminderId);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Done Action
        Intent doneIntent = new Intent(context, ReminderBroadcastReceiver.class);
        doneIntent.setAction("com.example.dailyreminder.ACTION_DONE");
        doneIntent.putExtra("title", title);
        doneIntent.putExtra("description", description);
        doneIntent.putExtra("reminderId", reminderId);
        PendingIntent donePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId + 1, // Unique ID for the Done action
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Delete Action
        Intent deleteIntent = new Intent(context, ReminderBroadcastReceiver.class);
        deleteIntent.setAction("com.example.dailyreminder.ACTION_DELETE");
        deleteIntent.putExtra("reminderId", reminderId);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId + 2, // Unique ID for the Delete action
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_snooze, "Snooze", snoozePendingIntent)
                .addAction(R.drawable.ic_done, "Done", donePendingIntent)
                .addAction(R.drawable.ic_delete, "Delete", deletePendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(reminderId, builder.build());
        }
    }


    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }


    public static void requestExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            context.startActivity(intent);
        }
    }
}