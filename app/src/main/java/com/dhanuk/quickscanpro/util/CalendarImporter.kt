package com.dhanuk.quickscanpro.util

import android.content.ContentUris
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import com.dhanuk.quickscanpro.database.CalendarEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Parse a calendar event out of a vEvent/vCalendar/ICS-style QR payload
 * and write it to the user's local Google calendar via CalendarContract.
 *
 * Pure local. No network. No paid APIs.
 */
object CalendarImporter {

    data class ParsedEvent(
        val title: String,
        val location: String = "",
        val startMs: Long,
        val endMs: Long? = null,
        val description: String = ""
    )

    private val icsDateFmt = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.US)
    private val icsDateFmtNoTime = SimpleDateFormat("yyyyMMdd", Locale.US)
    private val humanDateTimeFmt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    /** Accepts ICS-style raw text OR a free-text natural description. */
    fun parse(rawText: String): ParsedEvent? {
        val t = rawText.trim()
        if (t.isEmpty()) return null

        // ICS / vCalendar case
        if (t.startsWith("BEGIN:VEVENT", ignoreCase = true) || t.startsWith("BEGIN:VCALENDAR", ignoreCase = true)) {
            return parseICS(t)
        }

        // Quick natural line format: "Event: <title> @ <location> | 2025-12-31 18:00"
        val naturalRegex = Regex("""^(?:Event|Reminder|Meet)\s*[:\-]\s*(.+?)(?:\s*@\s*([^|]+))?\s*(?:\|\s*(\d{4}-\d{2}-\d{2} \d{2}:\d{2})(?:\s*-\s*(\d{4}-\d{2}-\d{2} \d{2}:\d{2}))?)?$""", RegexOption.IGNORE_CASE)
        naturalRegex.find(t)?.let { m ->
            val title = m.groupValues[1].trim()
            val location = m.groupValues[2].trim()
            val startStr = m.groupValues[3]
            val endStr = m.groupValues[4]
            val start: Long = if (startStr.isNotBlank()) {
                runCatching { humanDateTimeFmt.parse(startStr)?.time }.getOrNull()
                    ?: (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1))
            } else {
                System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)
            }
            val end: Long = if (!endStr.isNullOrBlank()) {
                runCatching { humanDateTimeFmt.parse(endStr)?.time }.getOrNull() ?: (start + TimeUnit.HOURS.toMillis(1))
            } else {
                start + TimeUnit.HOURS.toMillis(1)
            }
            return ParsedEvent(title, location, start, end)
        }

        // Fallback: whole text as the title, fire 1h from now.
        return ParsedEvent(
            title = t.take(80),
            startMs = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1),
            endMs = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)
        )
    }

    private fun parseICS(text: String): ParsedEvent? {
        var title = ""
        var location = ""
        var description = ""
        var start: Long? = null
        var end: Long? = null
        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("SUMMARY:", ignoreCase = true) ->
                    title = unescapeICS(line.substringAfter(":"))
                line.startsWith("LOCATION:", ignoreCase = true) ->
                    location = unescapeICS(line.substringAfter(":"))
                line.startsWith("DESCRIPTION:", ignoreCase = true) ->
                    description = unescapeICS(line.substringAfter(":"))
                line.startsWith("DTSTART", ignoreCase = true) ->
                    start = parseICSDate(line.substringAfter(":"))
                line.startsWith("DTEND", ignoreCase = true) ->
                    end = parseICSDate(line.substringAfter(":"))
            }
        }
        if (title.isBlank() && start == null) return null
        return ParsedEvent(
            title = title.ifBlank { "Imported Event" },
            location = location,
            startMs = start ?: (System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1)),
            endMs = end,
            description = description
        )
    }

    private fun parseICSDate(s: String): Long? {
        val raw = s.trim()
        val isUtc = raw.endsWith("Z", ignoreCase = true)
        val v = raw.removeSuffix("Z").removeSuffix("z")
        val ms = runCatching { icsDateFmt.parse(v)?.time }.getOrNull()
            ?: runCatching { icsDateFmtNoTime.parse(v)?.time }.getOrNull()
            ?: return null
        return if (isUtc) {
            val tz = java.util.TimeZone.getTimeZone("UTC")
            val local = java.util.TimeZone.getDefault()
            ms - tz.getOffset(ms) + local.getOffset(ms)
        } else {
            ms
        }
    }

    private fun unescapeICS(s: String): String =
        s.replace("\\n", "\n").replace("\\,", ",").replace("\\;", ";").trim()

    /** Write to the user's primary calendar. Returns the event id on success, null on failure. */
    fun importToCalendar(context: Context, event: ParsedEvent): Long? {
        return try {
            val resolver: ContentResolver = context.contentResolver
            val values = ContentValues().apply {
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.DTSTART, event.startMs)
                put(CalendarContract.Events.DTEND, event.endMs ?: event.startMs + TimeUnit.HOURS.toMillis(1))
                put(CalendarContract.Events.CALENDAR_ID, getPrimaryCalendarId(resolver) ?: 1)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.Calendar.getInstance().timeZone.id)
            }
            val uri: Uri? = resolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.let { ContentUris.parseId(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun getPrimaryCalendarId(resolver: ContentResolver): Long? {
        return try {
            val cursor = resolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY),
                "${CalendarContract.Calendars.IS_PRIMARY} = 1",
                null,
                "${CalendarContract.Calendars._ID} ASC"
            )
            cursor?.use {
                if (it.moveToFirst()) it.getLong(0) else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Open the calendar app at a specific time. */
    fun openCalendar(context: Context, startMs: Long) {
        try {
            val builder = ContentUris.appendId(
                CalendarContract.CONTENT_URI.buildUpon().appendPath("time"),
                startMs
            )
            context.startActivity(Intent(Intent.ACTION_VIEW).setData(builder.build()))
        } catch (_: Exception) {
            android.widget.Toast.makeText(context, "No calendar app available", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
