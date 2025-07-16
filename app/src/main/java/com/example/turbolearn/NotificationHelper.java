package com.example.turbolearn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Data;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationHelper {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final String CHANNEL_NAME = "Task Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for task reminders";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void scheduleNotification(Context context, Task task, String taskId) {
        NotificationHelper.scheduleNotification(context, task, taskId, true, true, null);
    }

    // Method to schedule notification with alarm settings
    public static void scheduleNotification(Context context, Task task, String taskId,
                                            boolean enableAlarm, boolean enableVibration, String alarmTone) {
        // Check if task has a due date
        Date dueDate = task.getDueDate();
        if (dueDate == null) {
            return; // No due date, cannot schedule notification
        }

        long currentTime = System.currentTimeMillis();
        long taskTime = dueDate.getTime();
        long delay = taskTime - currentTime;

        // Only schedule if the due date is in the future
        if (delay > 0) {
            Data inputData = new Data.Builder()
                    .putString("task_title", task.getTitle())
                    .putString("task_description", task.getDescription())
                    .putString("task_id", taskId)
                    .putString("task_category", task.getCategory().getDisplayName())
                    .putString("task_priority", task.getPriority().getDisplayName())
                    .putBoolean("enable_alarm", enableAlarm)
                    .putBoolean("enable_vibration", enableVibration)
                    .putString("alarm_tone", alarmTone)
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(context).enqueue(notificationWork);
        }
    }

    // Overloaded method to schedule notification with custom reminder time
    public static void scheduleNotification(Context context, Task task, String taskId, long reminderMinutesBefore) {
        Date dueDate = task.getDueDate();
        if (dueDate == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        long taskTime = dueDate.getTime();
        long reminderTime = taskTime - (reminderMinutesBefore * 60 * 1000); // Convert minutes to milliseconds
        long delay = reminderTime - currentTime;

        if (delay > 0) {
            Data inputData = new Data.Builder()
                    .putString("task_title", task.getTitle())
                    .putString("task_description", task.getDescription())
                    .putString("task_id", taskId)
                    .putString("task_category", task.getCategory().getDisplayName())
                    .putString("task_priority", task.getPriority().getDisplayName())
                    .putLong("reminder_minutes", reminderMinutesBefore)
                    .build();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                    .setInputData(inputData)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .build();

            WorkManager.getInstance(context).enqueue(notificationWork);
        }
    }

    // Method to cancel scheduled notification
    public static void cancelNotification(Context context, String taskId) {
        WorkManager.getInstance(context).cancelAllWorkByTag(taskId);
    }

    // Method to reschedule notification when task is updated
    public static void rescheduleNotification(Context context, Task task, String taskId) {
        // Cancel existing notification
        cancelNotification(context, taskId);

        // Schedule new notification if task is not completed
        if (!task.isCompleted()) {
            scheduleNotification(context, task, taskId);
        }
    }

    // Method to reschedule notification with custom reminder time
    public static void rescheduleNotification(Context context, Task task, String taskId, long reminderMinutesBefore) {
        cancelNotification(context, taskId);

        if (!task.isCompleted()) {
            scheduleNotification(context, task, taskId, reminderMinutesBefore);
        }
    }

    // Getter for channel ID (useful for NotificationWorker)
    public static String getChannelId() {
        return CHANNEL_ID;
    }
}