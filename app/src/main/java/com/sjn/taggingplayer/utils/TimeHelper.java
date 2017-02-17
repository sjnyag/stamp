package com.sjn.taggingplayer.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

import java.util.Date;

public class TimeHelper {

    public static DateTime getJapanNow() {
        return new DateTime(DateTimeZone.forID("Asia/Tokyo"));
    }

    public static String toRFC3339(long unixTime) {
        return ISODateTimeFormat.dateTime().print(new DateTime(new Date(unixTime * 1000L)));
    }

    public static DateTime toDateTime(String rfc3339) {
        return ISODateTimeFormat.dateTime().parseDateTime(rfc3339);
    }
}
