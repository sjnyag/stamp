package com.sjn.stamp.utils;

import android.content.Context;
import android.content.res.Resources;

import com.sjn.stamp.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    public static String getDateText(Date date, Resources resources) {
        DateTime dateTime = TimeHelper.toDateTime(date).minusSeconds(20);
        DateTime now = TimeHelper.getJapanNow();
        Minutes minutes = Minutes.minutesBetween(dateTime, now);
        if (minutes.isLessThan(Minutes.minutes(1))) {
            return resources.getString(R.string.item_song_history_seconds_ago, Seconds.secondsBetween(dateTime, now).getSeconds());
        } else if (minutes.isLessThan(Minutes.minutes(60))) {
            return resources.getString(R.string.item_song_history_minutes_ago, Minutes.minutesBetween(dateTime, now).getMinutes());
        } else {
            return TimeHelper.formatMMDDHHMM(dateTime);
        }
    }

    static String getDateDiff(Context context, Date before) {
        return getDateDiff(context, before, getJapanNow().toDate());
    }

    private static String getDateDiff(Context context, Date before, Date after) {
        DateTime dt1 = toDateTime(before);
        DateTime dt2 = toDateTime(after);
        if (dt1.isAfter(dt2)) {
            dt1 = toDateTime(after);
            dt2 = toDateTime(before);
        }
        int months = Months.monthsBetween(dt1, dt2).getMonths();
        if (months > 12) {
            int years = months / 12;
            months = months - years * 12;
            return months == 0 ? context.getResources().getQuantityString(R.plurals.date_diff_years, years, years) :
                    context.getResources().getQuantityString(R.plurals.date_diff_years, years, years) + " " + context.getResources().getQuantityString(R.plurals.date_diff_months, months, months);
        } else if (months > 0) {
            return context.getResources().getQuantityString(R.plurals.date_diff_months, months, months);
        }
        Duration d = new Duration(dt1, dt2);
        if (d.getStandardDays() > 1) {
            int days = (int) d.getStandardDays();
            return context.getResources().getQuantityString(R.plurals.date_diff_days, days, days);
        }
        if (d.getStandardHours() >= 1) {
            int hours = (int) d.getStandardHours();
            int minutes = (int) d.getStandardMinutes() - hours * 60;
            return context.getResources().getQuantityString(R.plurals.date_diff_hours, hours, hours) + " " + context.getResources().getQuantityString(R.plurals.date_diff_minutes, minutes, minutes);
        } else {
            int minutes = (int) d.getStandardMinutes();
            return context.getResources().getQuantityString(R.plurals.date_diff_minutes, minutes, minutes);
        }
    }

    public static Date toDateOnly(Date date) {
        return toDateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static LocalDate parse(String date) {
        return ISODateTimeFormat.localDateParser().parseLocalDate(date);
    }

    public static LocalDate getJapanToday() {
        return new LocalDate(new Date(System.currentTimeMillis()));
    }

    public static DateTime getJapanNow() {
        return new DateTime(new Date(System.currentTimeMillis()));
    }

    static String toRFC3339(long unixTime) {
        return ISODateTimeFormat.dateTime().print(new DateTime(new Date(unixTime * 1000L)));
    }

    public static String format(String dateTime) {
        return format(toDateTime(dateTime));
    }

    private static String format(DateTime dateTime) {
        return DateTimeFormat.mediumDateTime().print(dateTime);
    }

    public static String formatYYYYMMDDHHMMSS(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
    }

    public static String formatMMDDHHMM(Date date) {
        return new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(date);
    }

    public static String formatMMDDHHMM(DateTime date) {
        return new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()).format(date.toDate());
    }

    public static DateTime toDateTime(String rfc3339) {
        return ISODateTimeFormat.dateTime().parseDateTime(rfc3339);
    }

    public static DateTime toDateTime(Date date) {
        return new DateTime(date);
    }

    public static int getJapanMonth() {
        return getJapanNow().getMonthOfYear();
    }

    public static int getJapanYear() {
        return getJapanNow().getYear();
    }

    public static int getJapanDay() {
        return getJapanNow().getDayOfMonth();
    }

    public static LocalDate yearStart(int year) {
        return new LocalDate(year, 1, 1);
    }

    public static LocalDate yearEnd(int year) {
        return new LocalDate(year, 12, 31);
    }

    private static LocalDate yearMonthStart(int year, int month) {
        return new LocalDate(year, month, 1);
    }

    public static LocalDate yearMonthEnd(int year, int month) {
        return yearMonthStart(year, month).dayOfMonth().withMaximumValue();
    }
}
