package liquibase.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;

public class ISODateFormat {

    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT_STRING);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STRING);

    private static final String ISO_DATE_REGEX = "\\d{4}-\\d{2}-\\d{2}";
    private static final String ISO_TIME_REGEX = "\\d{2}:\\d{2}:\\d{2}";
    private static final String NANOS_SUFFIX_REGEX = "[.]\\d+";
    private static final String ZONE_SUFFIX_REGEX = "(Z|([-+]?\\d{2}:\\d{2}))";

    private static final String DATE_TIME_REGEX = ISO_DATE_REGEX + "T" + ISO_TIME_REGEX;
    private static final String DATE_TIME_WITH_NANOS_REGEX = DATE_TIME_REGEX + NANOS_SUFFIX_REGEX;
    private static final String DATE_TIME_WITH_ZONE_REGEX = DATE_TIME_REGEX + ZONE_SUFFIX_REGEX;
    private static final String DATE_TIME_WITH_NANOS_AND_ZONE_REGEX = DATE_TIME_WITH_NANOS_REGEX + ZONE_SUFFIX_REGEX;

    private static final String TIME_FORMAT_STRING = "HH:mm:ss";
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT_STRING = DATE_FORMAT_STRING + "'T'" + TIME_FORMAT_STRING;

    public String format(java.sql.Date date) {
        return dateFormat.format(date);
    }

    public String format(java.sql.Time date) {
        return timeFormat.format(date);
    }

    public String format(java.sql.Timestamp date) {
        StringBuilder sb = new StringBuilder(dateTimeFormat.format(date));
        int nanos = date.getNanos();
        if (nanos != 0) {
            String nanosString = String.format("%09d", nanos);
            int lastNotNullIndex = 8;
            for (; lastNotNullIndex > 0; lastNotNullIndex--) {
                if (nanosString.charAt(lastNotNullIndex) != '0') {
                    break;
                }
            }
            sb.append('.');
            sb.append(nanosString, 0, lastNotNullIndex + 1);
        }
        return sb.toString();
    }

    public String format(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return format(((java.sql.Date) date));
        } else if (date instanceof Time) {
            return format(((java.sql.Time) date));
        } else if (date instanceof java.sql.Timestamp) {
            return format(((java.sql.Timestamp) date));
        } else {
            return format(new java.sql.Timestamp(date.getTime()));
        }
    }

    public Date parse(String dateAsString) throws ParseException {
        if (dateAsString == null) {
            return null;
        }
        final int length = dateAsString.length();
        if (length == 8) {
            return new java.sql.Time(timeFormat.parse(dateAsString).getTime());
        }

        if (length == 10) {
            return new java.sql.Date(dateFormat.parse(dateAsString).getTime());
        }

        final String dateAsStringFixed = dateAsString.replace(" ", "T");
        if (dateAsStringFixed.matches(DATE_TIME_WITH_NANOS_AND_ZONE_REGEX)) {
            return parseZonedDateTime(dateAsStringFixed);
        }

        if (dateAsStringFixed.matches(DATE_TIME_WITH_NANOS_REGEX)) {
            return parseLocalDateTimeWithNanos(dateAsStringFixed + "+00:00");
        }

        if (dateAsStringFixed.matches(DATE_TIME_WITH_ZONE_REGEX)) {
            return parseZonedDateTime(dateAsStringFixed);
        }

        if (dateAsStringFixed.matches(DATE_TIME_REGEX)) {
            return new java.sql.Timestamp(dateTimeFormat.parse(dateAsStringFixed).getTime());
        }

        throw new ParseException(String.format("Unknown date format to parse: %s.", dateAsString), 0);
    }

    private static Timestamp parseLocalDateTimeWithNanos(String dateAsString) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateAsString)
                        .withZoneSameLocal(ZoneId.systemDefault());

        return toTimestamp(zonedDateTime);
    }

    private static Timestamp parseZonedDateTime(String dateAsString) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateAsString)
                        .withZoneSameInstant(ZoneId.systemDefault());

        return toTimestamp(zonedDateTime);
    }

    private static Timestamp toTimestamp(ZonedDateTime zonedDateTime) {
        long millis = 1000L * zonedDateTime.toEpochSecond();
        Timestamp timestamp = new Timestamp( millis );
        timestamp.setNanos( zonedDateTime.getNano() );
        return timestamp;
    }
}
