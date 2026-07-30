package com.dhanuk.quickscanpro.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dhanuk.quickscanpro.receiver.ReminderReceiver

/**
 * Schedules and cancels scan-result reminders via the AlarmManager, with exact
 * timing where possible. Reminders fire a lock-screen notification through
 * [ReminderReceiver].
 *
 * Uses free, always-available Android system services — no FCM, no servers.
 */
object ReminderScheduler {

    private const val PREFIX = "qr_reminder_"

    private fun piFor(context: Context, scanId: Int, content: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "$PREFIX$scanId"
            putExtra(ReminderReceiver.EXTRA_SCAN_ID, scanId)
            putExtra(ReminderReceiver.EXTRA_CONTENT, content)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, scanId, intent, flags)
    }

    fun schedule(context: Context, scanId: Int, content: String, triggerAtMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = piFor(context, scanId, content)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
            }
        } catch (se: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancel(context: Context, scanId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = piFor(context, scanId, "")
        am.cancel(pi)
    }

    fun relativeOptions(): List<Pair<String, Long>> {
        val now = System.currentTimeMillis()
        return listOf(
            "In 1 hour" to now + 3_600_000L,
            "In 3 hours" to now + 10_800_000L,
            "Tomorrow this time" to now + 86_400_000L,
            "In 3 days" to now + 3 * 86_400_000L,
            "In 1 week" to now + 7 * 86_400_000L
        )
    }
}
