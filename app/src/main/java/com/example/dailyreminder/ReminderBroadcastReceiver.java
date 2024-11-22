package com.example.dailyreminder;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        int reminderId = intent.getIntExtra("reminderId", 0);


        if ("com.example.dailyreminder.ACTION_SNOOZE".equals(action)) {
            handleSnooze(context, title, description, reminderId);
            return;
        }

        if ("com.example.dailyreminder.ACTION_DONE".equals(action)) {
            Log.d("ReminderBroadcastReceiver", "Marking as done: " + reminderId);
            markReminderAsDone(context, reminderId, title);
            return;
        }

        if ("com.example.dailyreminder.ACTION_DELETE".equals(action)) {
            Log.d("ReminderBroadcastReceiver", "Deleting reminder: " + reminderId);
            deleteReminder(context, reminderId);
            return;
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(reminderId + 1);
        }
        Log.d("ReminderBroadcastReceiver", "Unknown action: " + action + ", showing notification");
        ReminderNotificationHelper.showNotificationWithSnooze(context, title, description, reminderId);
    }

    private void handleSnooze(Context context, String title, String description, int reminderId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(reminderId);
        }

        long snoozeTimeMillis = System.currentTimeMillis() + 1 * 60 * 1000; // 1 minute snooze
        String snoozeTimeFormatted = android.text.format.DateFormat.format("hh:mm a", snoozeTimeMillis).toString();
        scheduleSnooze(context, title, description, reminderId, 1);

        showSnoozedNotification(context, snoozeTimeFormatted, reminderId);
    }

    private void scheduleSnooze(Context context, String title, String description, int reminderId, int snoozeMinutes) {
        try {
            if (!ReminderNotificationHelper.canScheduleExactAlarms(context)) {
                ReminderNotificationHelper.requestExactAlarmPermission(context);
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent snoozeIntent = new Intent(context, ReminderBroadcastReceiver.class);
            snoozeIntent.putExtra("title", title);
            snoozeIntent.putExtra("description", description);
            snoozeIntent.putExtra("reminderId", reminderId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reminderId,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            long snoozeTimeMillis = System.currentTimeMillis() + snoozeMinutes * 60 * 1000;

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTimeMillis, pendingIntent);
            }

            String snoozeTimeFormatted = android.text.format.DateFormat.format("hh:mm a", snoozeTimeMillis).toString();
            Toast.makeText(context, "Alarm will go off at " + snoozeTimeFormatted, Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            Log.e("ReminderBroadcastReceiver", "Permission denied for scheduling alarms.", e);
        }
    }

    private void showSnoozedNotification(Context context, String snoozeTimeFormatted, int reminderId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent dismissIntent = new Intent(context, ReminderBroadcastReceiver.class);
        dismissIntent.setAction("com.example.dailyreminder.ACTION_DISMISS");
        dismissIntent.putExtra("reminderId", reminderId);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Alarm Snoozed")
                .setContentText("Alarm will go off at " + snoozeTimeFormatted)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(reminderId + 1, builder.build());
        }
    }

    private void markReminderAsDone(Context context, int reminderId, String title) {

        Intent doneIntent = new Intent("com.example.dailyreminder.ACTION_DONE");
        doneIntent.putExtra("reminderId", reminderId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(doneIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(reminderId);
        }


        Toast.makeText(context, title + " marked as done.", Toast.LENGTH_SHORT).show();
    }

    private void deleteReminder(Context context, int reminderId) {

        Intent deleteIntent = new Intent("com.example.dailyreminder.ACTION_DELETE");
        deleteIntent.putExtra("reminderId", reminderId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(deleteIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(reminderId);
        }


        Toast.makeText(context, "Reminder deleted.", Toast.LENGTH_SHORT).show();
    }
}