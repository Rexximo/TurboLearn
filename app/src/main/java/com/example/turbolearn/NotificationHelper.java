package com.example.turbolearn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateTimeString = task.getDate() + " " + task.getTime();
            Date taskDateTime = dateFormat.parse(dateTimeString);

            if (taskDateTime != null) {
                long currentTime = System.currentTimeMillis();
                long taskTime = taskDateTime.getTime();
                long delay = taskTime - currentTime;

                if (delay > 0) {
                    Data inputData = new Data.Builder()
                            .putString("task_title", task.getTitle())
                            .putString("task_description", task.getDescription())
                            .putString("task_id", taskId)
                            .build();

                    OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                            .setInputData(inputData)
                            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                            .build();

                    WorkManager.getInstance(context).enqueue(notificationWork);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}