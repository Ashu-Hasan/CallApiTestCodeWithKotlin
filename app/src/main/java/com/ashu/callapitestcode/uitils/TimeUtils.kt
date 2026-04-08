package com.ashu.callapitestcode.uitils

import android.graphics.Insets
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import androidx.annotation.ColorInt
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object TimeUtils {

    // ✅ Known formats
    private val knownFormats = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd hh:mm:ss a",
        "yyyy-MM-dd hh:mm a",
        "dd-MM-yyyy HH:mm:ss",
        "dd-MM-yyyy HH:mm",
        "dd-MM-yyyy hh:mm:ss a",
        "dd-MM-yyyy hh:mm a",
        "MM/dd/yyyy HH:mm:ss",
        "MM/dd/yyyy hh:mm:ss a",
        "dd MMM yyyy HH:mm:ss",
        "dd MMM yyyy hh:mm:ss a",
        "EEE MMM dd HH:mm:ss z yyyy",
        "dd/MM/yyyy HH:mm:ss",
        "dd/MM/yyyy hh:mm:ss a",
        "dd/MM/yyyy HH:mm",
        "dd/MM/yyyy hh:mm a",
        "dd MMM yyyy, hh:mm a",
        "dd-MMM-yyyy",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "MM/dd/yyyy",
        "dd MMM yyyy",
        "dd/MM/yyyy",
        "HH:mm:ss",
        "HH:mm",
        "hh:mm:ss a",
        "hh:mm a"
    )

    // ✅ Safe parser
    fun parseDateSafely(dateString: String?): Date? {
        if (dateString.isNullOrBlank()) return null

        for (format in knownFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault()).apply {
                    isLenient = false
                }
                sdf.parse(dateString)?.let { return it }
            } catch (_: Exception) {
            }
        }
        return null
    }

    // ✅ Time ago
    fun getTimeAgo(dateString: String): String {
        val pastDate = parseDateSafely(dateString) ?: return "Invalid date"

        val diff = Date().time - pastDate.time
        if (diff < 0) return "In the future"

        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            days == 1L -> "Yesterday"
            days < 30 -> "$days days ago"
            days < 365 -> "${days / 30} month${if ((days / 30) == 1L) "" else "s"} ago"
            else -> "${days / 365} year${if ((days / 365) == 1L) "" else "s"} ago"
        }
    }

    // ✅ Convert format
    fun convertDateFormat(date: String, outputFormat: String): String {
        val parsed = parseDateSafely(date) ?: return date
        return SimpleDateFormat(outputFormat, Locale.getDefault()).format(parsed)
    }

    // ✅ To millis
    fun convertDateToMillis(date: String, format: String?): Long {
        return try {
            val parsed = if (!format.isNullOrEmpty()) {
                SimpleDateFormat(format, Locale.getDefault()).parse(date)
            } else {
                parseDateSafely(date)
            }
            parsed?.time ?: 0
        } catch (_: Exception) {
            0
        }
    }

    // ✅ Timestamp → date
    fun convertTimestampIntoDate(value: Long, type: String): String {
        val timestamp = if (value < 10000000000L) value * 1000 else value
        val date = Date(timestamp)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault())

        return when (type.lowercase()) {
            "time" -> timeFormat.format(date)
            "date" -> dateFormat.format(date)
            else -> dateTimeFormat.format(date)
        }
    }

    // ✅ 24 → 12 hour
    fun convert24HourTo12Hour(time: String): String? {
        val parsed = parseDateSafely(time) ?: return null
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(parsed)
    }

    fun getCurrentDate(format: String): String {
        return SimpleDateFormat(format, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }.format(Date())
    }

    // ✅ Expiry check
    fun isDateExpired(current: String, target: String): Boolean {
        val currentDate = parseDateSafely(current)
        val targetDate = parseDateSafely(target)
        return currentDate == null || targetDate == null || targetDate.before(currentDate)
    }

    // ✅ Days difference
    fun getDaysDifference(start: String, end: String): Long {
        val s = parseDateSafely(start) ?: return 0
        val e = parseDateSafely(end) ?: return 0
        return TimeUnit.MILLISECONDS.toDays(kotlin.math.abs(e.time - s.time))
    }

    // ✅ Status bar color
    fun setHomeStatusBarColor(window: Window, @ColorInt color: Int) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {

            val decorView = window.decorView

            decorView.setOnApplyWindowInsetsListener { view, insets ->

                val statusBarInsets: Insets =
                    insets.getInsets(WindowInsets.Type.statusBars())

                view.setBackgroundColor(color)

                view.setPadding(
                    0,
                    statusBarInsets.top,
                    0,
                    0
                )

                insets
            }

        } else {
            window.statusBarColor = color
        }
    }
}