package com.sjn.stamp.utils;

import android.content.Context;

import com.sjn.stamp.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    public static String getDateDiff(Context context, Date before) {
        return getDateDiff(context, before, getJapanNow().toDate());
    }

    public static String getDateDiff(Context context, Date before, Date after) {
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
            return months == 0 ? context.getResources().getString(R.string.date_diff_years, String.valueOf(years)) :
                    context.getResources().getString(R.string.date_diff_years_and_months, String.valueOf(years), String.valueOf(months));
        } else if (months > 0) {
            return context.getResources().getString(R.string.date_diff_months, String.valueOf(months));
        }
        Duration d = new Duration(dt1, dt2);
        if (d.getStandardDays() > 1) {
            return context.getResources().getString(R.string.date_diff_days, String.valueOf(d.getStandardDays()));
        }
        if (d.getStandardHours() >= 1) {
            return context.getResources().getString(R.string.date_diff_hours_and_minutes, String.valueOf(d.getStandardHours()), String.valueOf(d.getStandardMinutes() - ((int) d.getStandardHours()) * 60));
        } else {
            return context.getResources().getString(R.string.date_diff_minutes, String.valueOf(d.getStandardMinutes()));
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

    public static String toRFC3339(long unixTime) {
        return ISODateTimeFormat.dateTime().print(new DateTime(new Date(unixTime * 1000L)));
    }

    public static String format(String dateTime) {
        return format(toDateTime(dateTime));
    }

    public static String format(DateTime dateTime) {
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

    public static LocalDate yearMonthStart(int year, int month) {
        return new LocalDate(year, month, 1);
    }

    public static LocalDate yearMonthEnd(int year, int month) {
        return yearMonthStart(year, month).dayOfMonth().withMaximumValue();
    }
}
