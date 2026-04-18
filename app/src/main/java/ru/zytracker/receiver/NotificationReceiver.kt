package ru.zytracker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import ru.zytracker.MainActivity
import ru.zytracker.R

/**
 * BroadcastReceiver для обработки уведомлений
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "zy_tracker_notification"
        const val NOTIFICATION_ID = 1001
        const val ACTION_NOTIFICATION = "ru.zytracker.NOTIFICATION"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_NOTIFICATION) {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Создаём канал для уведомлений (Android 8.0+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Ежедневные напоминания о внесении данных"
        }
        notificationManager.createNotificationChannel(channel)

        // Интент для открытия приложения при нажатии на уведомление
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Создаём уведомление
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ЗЯ Трекер")
            .setContentText("Не забудьте внести данные за сегодня!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
