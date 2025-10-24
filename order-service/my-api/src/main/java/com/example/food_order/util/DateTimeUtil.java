package com.example.food_order.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    public static final ZoneId MY_TIMEZONE = ZoneId.of("Asia/Bangkok");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static String formatDateTime(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    public static String formatZoneDateTime(ZonedDateTime dateTime) {
        ZonedDateTime bangkokTime = dateTime.withZoneSameInstant(MY_TIMEZONE);
        return bangkokTime.format(DATE_TIME_FORMATTER);
    }
}
