package com.sjn.taggingplayer.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

public class TimeHelper {

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
