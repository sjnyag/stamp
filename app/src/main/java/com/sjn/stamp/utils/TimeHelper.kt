package com.sjn.stamp.utils

import android.content.Context
import android.content.res.Resources
import com.sjn.stamp.R
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
object TimeHelper {

    val japanToday: LocalDate
        get() = LocalDate(Date(System.currentTimeMillis()))

    val japanNow: DateTime
        get() = DateTime(Date(System.currentTimeMillis()))

    val japanMonth: Int
        get() = japanNow.monthOfYear

    val japanYear: Int
        get() = japanNow.year

    val japanDay: Int
        get() = japanNow.dayOfMonth

    fun getDateText(date: Date, resources: Resources): String {
        val dateTime = TimeHelper.toDateTime(date).minusSeconds(20)
        val now = TimeHelper.japanNow
        val minutes = Minutes.minutesBetween(dateTime, now)
        return when {
            minutes.isLessThan(Minutes.minutes(1)) -> resources.getString(R.string.item_song_history_seconds_ago, Seconds.secondsBetween(dateTime, now).seconds)
            minutes.isLessThan(Minutes.minutes(60)) -> resources.getString(R.string.item_song_history_minutes_ago, Minutes.minutesBetween(dateTime, now).minutes)
            else -> TimeHelper.formatMMDDHHMM(dateTime)
        }
    }

    fun getDateDiff(context: Context, before: Date): String {
        return getDateDiff(context, before, japanNow.toDate())
    }

    fun getDateDiff(context: Context, before: Date, after: Date): String {
        var dt1 = toDateTime(before)
        var dt2 = toDateTime(after)
        if (dt1.isAfter(dt2)) {
            dt1 = toDateTime(after)
            dt2 = toDateTime(before)
        }
        var months = Months.monthsBetween(dt1, dt2).months
        if (months > 12) {
            val years = months / 12
            months -= years * 12
            return if (months == 0) context.resources.getQuantityString(R.plurals.date_diff_years, years, years) else context.resources.getQuantityString(R.plurals.date_diff_years, years, years) + " " + context.resources.getQuantityString(R.plurals.date_diff_months, months, months)
        } else if (months > 0) {
            return context.resources.getQuantityString(R.plurals.date_diff_months, months, months)
        }
        val d = Duration(dt1, dt2)
        if (d.standardDays > 1) {
            val days = d.standardDays.toInt()
            return context.resources.getQuantityString(R.plurals.date_diff_days, days, days)
        }
        return if (d.standardHours >= 1) {
            val hours = d.standardHours.toInt()
            val minutes = d.standardMinutes.toInt() - hours * 60
            context.resources.getQuantityString(R.plurals.date_diff_hours, hours, hours) + " " + context.resources.getQuantityString(R.plurals.date_diff_minutes, minutes, minutes)
        } else {
            val minutes = d.standardMinutes.toInt()
            context.resources.getQuantityString(R.plurals.date_diff_minutes, minutes, minutes)
        }
    }

    fun toDateOnly(date: Date): Date = toDateTime(date).withTimeAtStartOfDay().toDate()

    fun parse(date: String): LocalDate = ISODateTimeFormat.localDateParser().parseLocalDate(date)

    fun toRFC3339(unixTime: Long): String = ISODateTimeFormat.dateTime().print(DateTime(Date(unixTime * 1000L)))

    fun format(dateTime: String): String = format(toDateTime(dateTime))

    private fun format(dateTime: DateTime): String = DateTimeFormat.mediumDateTime().print(dateTime)

    fun formatYYYYMMDDHHMMSS(date: Date): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)

    fun formatMMDDHHMM(date: Date): String = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(date)

    fun formatMMDDHHMM(date: DateTime): String = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(date.toDate())

    fun toDateTime(rfc3339: String): DateTime = ISODateTimeFormat.dateTime().parseDateTime(rfc3339)

    fun toDateTime(date: Date): DateTime = DateTime(date)

    fun yearStart(year: Int): LocalDate = LocalDate(year, 1, 1)

    fun yearEnd(year: Int): LocalDate = LocalDate(year, 12, 31)

    private fun yearMonthStart(year: Int, month: Int): LocalDate = LocalDate(year, month, 1)

    fun yearMonthEnd(year: Int, month: Int): LocalDate = yearMonthStart(year, month).dayOfMonth().withMaximumValue()
}
