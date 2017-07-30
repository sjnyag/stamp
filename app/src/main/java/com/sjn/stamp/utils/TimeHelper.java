package com.sjn.stamp.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

public class TimeHelper {

    public static String getDateDiff(Date before) {
        return getDateDiff(before, getJapanNow().toDate());
    }

    public static String getDateDiff(Date before, Date after) {
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
            return months == 0 ? years + "年" : years + "年" + months + "ヶ月";
        } else if (months > 0) {
            return months + "ヶ月";
        }
        Duration d = new Duration(dt1, dt2);
        if (d.getStandardDays() > 1) {
            return ((int) d.getStandardDays()) + "日";
        }
        String result = "";
        int minutes = (int) d.getStandardMinutes();
        if (d.getStandardHours() >= 1) {
            result += ((int) d.getStandardHours()) + "時間";
            minutes = minutes - ((int) d.getStandardHours()) * 60;
        }
        result += minutes + "分";
        return result;
    }

    public static Date toDateOnly(Date date) {
        return toDateTime(date).withTimeAtStartOfDay().toDate();
    }

    public static LocalDate parse(String date) {
        return ISODateTimeFormat.localDateParser().parseLocalDate(date);
    }

    public static LocalDate getJapanToday() {
        return new LocalDate(DateTimeZone.forID("Asia/Tokyo"));
    }

    public static DateTime getJapanNow() {
        return new DateTime(DateTimeZone.forID("Asia/Tokyo"));
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
