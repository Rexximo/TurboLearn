package com.example.turbolearn;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String taskId = intent.getStringExtra("task_id");

        if (taskId == null) {
            return;
        }

        switch (action) {
            case "DISMISS_ALARM":
                handleDismissAlarm(context, taskId);
                break;
            case "COMPLETE_TASK":
                handleCompleteTask(context, taskId);
                break;
            case "SNOOZE_TASK":
                handleSnoozeTask(context, taskId);
                break;
        }
    }

    private void handleDismissAlarm(Context context, String taskId) {
        // Stop the alarm sound
        NotificationWorker.stopAlarm();

        // Cancel the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(taskId.hashCode());
        }

        Toast.makeText(context, "Alarm dismissed", Toast.LENGTH_SHORT).show();
    }

    private void handleCompleteTask(Context context, String taskId) {
        // Stop alarm first
        NotificationWorker.stopAlarm();

        // Cancel notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(taskId.hashCode());
        }

        // Cancel any pending notifications for this task
        NotificationHelper.cancelNotification(context, taskId);

        Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show();

        // TODO: Integrate with your database/repository to actually update the task
        // Example: TaskRepository.getInstance().markTaskAsCompleted(taskId);
    }

    private void handleSnoozeTask(Context context, String taskId) {
        // Stop alarm first
        NotificationWorker.stopAlarm();

        // Cancel current notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(taskId.hashCode());
        }

        // Cancel and reschedule notification
        NotificationHelper.cancelNotification(context, taskId);

        Toast.makeText(context, "Task snoozed for 15 minutes", Toast.LENGTH_SHORT).show();

        // TODO: Integrate with your database/repository to reschedule the task
        // Example: TaskRepository.getInstance().snoozeTask(taskId, 15);
    }
}