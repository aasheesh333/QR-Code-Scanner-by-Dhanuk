package com.dhanuk.quickscanpro.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dhanuk.quickscanpro.R

/**
 * Fires a scan-result reminder notification when the AlarmManager-wrapped
 * PendingIntent attached by [com.dhanuk.quickscanpro.util.ReminderScheduler]
 * fires.
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_SCAN_ID = "scan_id"
        const val EXTRA_CONTENT = "content"
        private const val CHANNEL_ID = "qr_scan_reminders"
        private const val CHANNEL_NAME = "Scan Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: "Scan result"
        val id = intent.getIntExtra(EXTRA_SCAN_ID, 0)
        ensureChannel(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("QuickScan Pro Reminder")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1000 + id, notif)
    }

    private fun ensureChannel(context: Context) {
        ensureChannelInternal(context)
    }

    internal fun ensureChannelInternal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val ch = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(ch)
        }
    }
}
