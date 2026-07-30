package com.dhanuk.quickscanpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-arms reminders after a device reboot. Ensures the notification channel
 * exists; actual re-scheduling is done lazily by the ViewModel on next app
 * open, since the QR app is foreground-most of the time.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        ReminderReceiver().ensureChannelInternal(context)
    }
}
