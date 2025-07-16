package com.example.turbolearn;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TaskActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String taskId = intent.getStringExtra("task_id");

        if (taskId == null) {
            return;
        }

        switch (action) {
            case "COMPLETE_TASK":
                handleCompleteTask(context, taskId);
                break;
            case "SNOOZE_TASK":
                handleSnoozeTask(context, taskId);
                break;
        }
    }

    private void handleCompleteTask(Context context, String taskId) {
        // Here you would typically:
        // 1. Update the task in your database to mark it as completed
        // 2. Cancel any pending notifications for this task
        // 3. Show a confirmation toast

        NotificationHelper.cancelNotification(context, taskId);
        Toast.makeText(context, "Task marked as completed", Toast.LENGTH_SHORT).show();

        // TODO: Integrate with your database/repository to actually update the task
        // Example: TaskRepository.getInstance().markTaskAsCompleted(taskId);
    }

    private void handleSnoozeTask(Context context, String taskId) {
        // Here you would typically:
        // 1. Reschedule the notification for a later time (e.g., 15 minutes)
        // 2. Show a confirmation toast

        NotificationHelper.cancelNotification(context, taskId);
        Toast.makeText(context, "Task snoozed for 15 minutes", Toast.LENGTH_SHORT).show();

        // TODO: Integrate with your database/repository to reschedule the task
        // Example: TaskRepository.getInstance().snoozeTask(taskId, 15);
    }
}