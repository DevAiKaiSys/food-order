package com.example.food_order.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public static String formatDateTime(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : null;
    }

    public static String formatZoneDateTime(ZonedDateTime dateTime) {
        ZonedDateTime bangkokTime = dateTime.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
        return bangkokTime.format(DATE_TIME_FORMATTER);
    }
}
