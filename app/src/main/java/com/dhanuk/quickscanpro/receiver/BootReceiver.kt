package com.dhanuk.quickscanpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dhanuk.quickscanpro.database.AppDatabase
import com.dhanuk.quickscanpro.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "BootReceiver"

/**
 * Re-arms reminders after a device reboot. AlarmManager alarms are cleared
 * on reboot, so any pending scan-result reminders must be re-scheduled from
 * the database rows that still have a non-null reminder_time in the future.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderReceiver().ensureChannelInternal(context)

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).scanResultDao()
                val pending = dao.getPendingReminders(System.currentTimeMillis())
                for (scan in pending) {
                    ReminderScheduler.schedule(
                        context, scan.id, scan.content, scan.reminderTime!!
                    )
                }
                Log.i(TAG, "Rescheduled ${pending.size} reminder(s) after boot")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule reminders after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
