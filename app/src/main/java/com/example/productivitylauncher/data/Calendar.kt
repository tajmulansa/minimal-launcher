package com.example.productivitylauncher.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AgendaItem(val title: String, val begin: Long, val end: Long, val allDay: Boolean)

fun hasCalendarPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

/** Today's calendar events, read from the device calendar (needs the optional calendar permission). */
fun todayEvents(context: Context): List<AgendaItem> {
    if (!hasCalendarPermission(context)) return emptyList()
    val start = startOfDay()
    val end = start + DAY_MS
    val projection = arrayOf(
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.ALL_DAY,
    )
    val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
        android.content.ContentUris.appendId(it, start)
        android.content.ContentUris.appendId(it, end)
    }.build()
    val out = ArrayList<AgendaItem>()
    runCatching {
        context.contentResolver.query(uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
            while (c.moveToNext() && out.size < 12) {
                out.add(AgendaItem(c.getString(0) ?: "(no title)", c.getLong(1), c.getLong(2), c.getInt(3) == 1))
            }
        }
    }
    return out
}

fun clockLabel(ms: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
