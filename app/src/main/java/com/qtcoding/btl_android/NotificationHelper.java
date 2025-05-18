package com.qtcoding.btl_android;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationHelper extends BroadcastReceiver {

    private static final String CHANNEL_ID = "StudyReminderChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_CODE = 1001;
    private static final String ACTION_STUDY_REMINDER = "com.qtcoding.memoapp.STUDY_REMINDER";

    public static void scheduleDailyNotification(Context context, int hour, int minute, int studyDuration) {
        Log.d("NotificationHelper", "Scheduling notification for " + hour + ":" + minute + ", duration: " + studyDuration);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationHelper.class);
        intent.setAction(ACTION_STUDY_REMINDER); // Thêm action tùy chỉnh

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Nếu thời gian đã qua hôm nay thì đặt cho ngày mai
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Log.d("NotificationHelper", "Time passed, scheduling for next day: " + calendar.getTime());
        } else {
            Log.d("NotificationHelper", "Scheduled for today: " + calendar.getTime());
        }

        // setExactAndAllowWhileIdle để chính xác hơn
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("NotificationHelper", "Exact alarms not allowed. Consider prompting user.");
                return;
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e("NotificationHelper", "Failed to schedule exact alarm: " + e.getMessage());
        }

        // Lưu lại vào SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putInt("notificationHour", hour)
                .putInt("notificationMinute", minute)
                .putInt("studyDuration", studyDuration)
                .apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "null";
        Log.d("NotificationHelper", "onReceive called with action: " + action);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            SharedPreferences prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE);
            int hour = prefs.getInt("notificationHour", 8);
            int minute = prefs.getInt("notificationMinute", 0);
            int studyDuration = prefs.getInt("studyDuration", 30);
            scheduleDailyNotification(context, hour, minute, studyDuration);
        } else if (ACTION_STUDY_REMINDER.equals(action)) {
            createNotificationChannel(context);
            SharedPreferences prefs = context.getSharedPreferences("StudyPrefs", Context.MODE_PRIVATE);
            int hour = prefs.getInt("notificationHour", 8);
            int minute = prefs.getInt("notificationMinute", 0);
            int studyDuration = prefs.getInt("studyDuration", 30);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String timeString = sdf.format(calendar.getTime());

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Study Reminder")
                    .setContentText("Time to study your vocabulary for " + studyDuration + " minutes at " + timeString + "!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

            // Đặt lại lịch cho ngày hôm sau
            scheduleDailyNotification(context, hour, minute, studyDuration);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Study Reminder";
            String description = "Daily study reminders";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
