package liquibase.util;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;

public class ISODateFormat {

    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT_STRING);
    private SimpleDateFormat dateTimeFormatWithSpace = new SimpleDateFormat(DATE_TIME_FORMAT_STRING_WITH_SPACE);
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String DATE_TIME_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String DATE_TIME_FORMAT_STRING_WITH_SPACE = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_TIME_FORMAT_STRING_WITH_SPACE_AND_NANOS = "yyyy-MM-dd HH:mm:ss.SS";


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
            sb.append(nanosString.substring(0, lastNotNullIndex + 1));
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
        } else if (date instanceof java.util.Date) {
            return format(new java.sql.Timestamp(date.getTime()));
        } else {
            throw new RuntimeException("Unknown type: "+date.getClass().getName());
        }
    }

    public Date parse(String dateAsString) throws ParseException {
        if (dateAsString == null) {
            return null;
        }
        int length = dateAsString.length();
        switch (length) {
        case 8:
            return new java.sql.Time(timeFormat.parse(dateAsString).getTime());
        case 10:
            return new java.sql.Date(dateFormat.parse(dateAsString).getTime());
        case 19:
            if (dateAsString.contains(" ")) {
                return new java.sql.Timestamp(dateTimeFormatWithSpace.parse(dateAsString).getTime());
            } else {
                return new java.sql.Timestamp(dateTimeFormat.parse(dateAsString).getTime());
            }
        default:
            if ((length < 19) || (dateAsString.charAt(19) != '.')) {
                throw new ParseException(String.format("Unknown date format to parse: %s.", dateAsString), 0);
            }
            long time = 0;
            if (dateAsString.contains(" ")) {
                time = dateTimeFormatWithSpace.parse(dateAsString.substring(0, 19)).getTime();
            } else {
                time = dateTimeFormat.parse(dateAsString.substring(0,19)).getTime();
            }

            ZonedDateTime zonedDateTime;
            int nanos;
            try {
                OffsetDateTime odt = OffsetDateTime.parse(dateAsString);
                zonedDateTime = odt.toZonedDateTime();
                nanos = zonedDateTime.getNano();
            }
            catch (DateTimeParseException dtpe) {
                if (dateAsString.contains(" ")) {
                    dateAsString = dateAsString.replaceAll(" ", "T");
                }
                DateTimeFormatter formatter =
                           new DateTimeFormatterBuilder()
                                .appendPattern(DATE_TIME_FORMAT_STRING)
                                .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 9, true)
                                .toFormatter();
                nanos = Integer.parseInt(dateAsString.substring(20));
                for (; length < 29; length++) {
                    nanos *= 10;
                }
            }


            /*
            */
            java.sql.Timestamp timestamp = new java.sql.Timestamp(time);
            timestamp.setNanos(nanos);
            return timestamp;
        }
    }
}