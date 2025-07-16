package com.example.turbolearn;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static MediaPlayer mediaPlayer;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String taskTitle = getInputData().getString("task_title");
        String taskDescription = getInputData().getString("task_description");
        String taskId = getInputData().getString("task_id");
        String taskCategory = getInputData().getString("task_category");
        String taskPriority = getInputData().getString("task_priority");
        boolean enableAlarm = getInputData().getBoolean("enable_alarm", false);
        boolean enableVibration = getInputData().getBoolean("enable_vibration", true);
        String alarmTone = getInputData().getString("alarm_tone");

        if (taskId == null || taskTitle == null) {
            return Result.failure();
        }

        showNotification(taskTitle, taskDescription, taskId, taskCategory, taskPriority,
                enableAlarm, enableVibration, alarmTone);

        return Result.success();
    }

    private void showNotification(String title, String description, String taskId,
                                  String category, String priority, boolean enableAlarm,
                                  boolean enableVibration, String alarmTone) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            return;
        }

        // Create intent to open the app when notification is tapped
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("task_id", taskId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                taskId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Create dismiss intent for stopping alarm
        Intent dismissIntent = new Intent(getApplicationContext(), AlarmActionReceiver.class);
        dismissIntent.setAction("DISMISS_ALARM");
        dismissIntent.putExtra("task_id", taskId);

        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                ("dismiss_" + taskId).hashCode(),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification content
        String contentTitle = "🔔 Task Reminder: " + title;
        String contentText = buildNotificationText(description, category, priority);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(getNotificationPriority(priority))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setOngoing(enableAlarm) // Makes notification persistent if alarm is enabled
                .setCategory(NotificationCompat.CATEGORY_ALARM);

        // Add dismiss action if alarm is enabled
        if (enableAlarm) {
            builder.addAction(R.drawable.ic_notification, "Stop Alarm", dismissPendingIntent);
        }

        // Handle vibration
        if (enableVibration) {
            triggerVibration();
            builder.setVibrate(new long[]{0, 500, 250, 500, 250, 500});
        }

        // Handle alarm sound
        if (enableAlarm) {
            playAlarmSound(alarmTone);
            builder.setSound(null); // We handle sound manually
        } else {
            // Use default notification sound
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            builder.setSound(defaultSoundUri);
        }

        notificationManager.notify(taskId.hashCode(), builder.build());
    }

    private String buildNotificationText(String description, String category, String priority) {
        StringBuilder text = new StringBuilder();

        if (description != null && !description.trim().isEmpty()) {
            text.append(description);
        }

        if (category != null && !category.trim().isEmpty()) {
            if (text.length() > 0) text.append(" • ");
            text.append("Category: ").append(category);
        }

        if (priority != null && !priority.trim().isEmpty()) {
            if (text.length() > 0) text.append(" • ");
            text.append("Priority: ").append(priority);
        }

        return text.length() > 0 ? text.toString() : "Task reminder";
    }

    private int getNotificationPriority(String priority) {
        if (priority == null) return NotificationCompat.PRIORITY_DEFAULT;

        switch (priority.toLowerCase()) {
            case "urgent":
                return NotificationCompat.PRIORITY_HIGH;
            case "high":
                return NotificationCompat.PRIORITY_HIGH;
            case "medium":
                return NotificationCompat.PRIORITY_DEFAULT;
            case "low":
                return NotificationCompat.PRIORITY_LOW;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private void triggerVibration() {
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Pattern: wait 0ms, vibrate 500ms, wait 250ms, vibrate 500ms, wait 250ms, vibrate 500ms
                long[] pattern = {0, 500, 250, 500, 250, 500};
                VibrationEffect effect = VibrationEffect.createWaveform(pattern, -1);
                vibrator.vibrate(effect);
            } else {
                // For older versions
                long[] pattern = {0, 500, 250, 500, 250, 500};
                vibrator.vibrate(pattern, -1);
            }
        }
    }

    private void playAlarmSound(String alarmTone) {
        try {
            Uri soundUri;

            if (alarmTone != null && !alarmTone.isEmpty()) {
                // Use custom alarm tone
                soundUri = Uri.parse(alarmTone);
            } else {
                // Use default alarm sound
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (soundUri == null) {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            }

            // Stop any currently playing alarm
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }

            mediaPlayer = MediaPlayer.create(getApplicationContext(), soundUri);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true); // Loop the alarm sound
                mediaPlayer.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default notification sound
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), defaultSoundUri);
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void stopAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}