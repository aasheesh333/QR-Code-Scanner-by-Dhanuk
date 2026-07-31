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
        val rawContent = intent.getStringExtra(EXTRA_CONTENT) ?: ""
        val id = intent.getIntExtra(EXTRA_SCAN_ID, 0)
        ensureChannel(context)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = if (launchIntent != null) {
            android.app.PendingIntent.getActivity(
                context, id, launchIntent,
                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else null

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("QuickScan Pro Reminder")
            .setContentText("You have a saved scan result to review")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("You have a saved scan result to review. Tap to open."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentIntent(pendingIntent)
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
