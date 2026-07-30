package com.dhanuk.quickscanpro.util

import com.dhanuk.quickscanpro.database.ScanResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Groups scans into a vertical "Timeline" of days for the new Timeline screen.
 */
object TimelineBuilder {

    data class TimelineDay(
        val dayLabel: String,
        val dateLabel: String,
        val items: List<ScanResult>
    )

    private val dayFmt = SimpleDateFormat("EEEE", Locale.getDefault())
    private val dateFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
    private val calKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun build(scans: List<ScanResult>): List<TimelineDay> {
        if (scans.isEmpty()) return emptyList()
        return scans
            .groupBy { calKeyFmt.format(Date(it.timestamp)) }
            .map { (dayKey, items) ->
                val cal = Calendar.getInstance()
                val date = calKeyFmt.parse(dayKey) ?: Date()
                cal.time = date
                TimelineDay(
                    dayLabel = relativeDayLabel(cal),
                    dateLabel = dateFmt.format(date),
                    items = items.sortedByDescending { it.timestamp }
                )
            }
            .sortedByDescending { it.items.first().timestamp }
    }

    private fun relativeDayLabel(cal: Calendar): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            sameDay(cal, today) -> "Today"
            sameDay(cal, yesterday) -> "Yesterday"
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> dayFmt.format(cal.time)
            else -> dayFmt.format(cal.time)
        }
    }

    private fun sameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
