/*
 * Copyright 2018 Sirsi Corporation.  All rights reserved.
 */

package liquibase.util;

import liquibase.exception.DateParseException;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles 'NOW' and 'TODAY' for Date / Time type columns
 */
public final class NowAndTodayUtil {
    private static final String NOW = "now";
    private static final int NOW_LENGTH = NOW.length();
    private static final Pattern NOW_OFFSET_PATTERN = Pattern.compile("^(now)([+-])(\\d+)([mhdy].*)$");
    private static final String TODAY = "today";
    private static final int TODAY_LENGTH = TODAY.length();
    private static final Pattern TODAY_OFFSET_PATTERN = Pattern.compile("^(today)([+\\-])(\\d+)$");

    /**
     * Private constructor to prevent instantiation.
     */
    private NowAndTodayUtil() {
    }

    /**
     * Checks if date starts with "NOW" or "TODAY".
     *
     * @param value value to test
     * @return true if value starts with "NOW" or "TODAY" (case insensitive), false otherwise.
     */
    public static boolean isNowOrTodayFormat(String value) {
        boolean ret = false;
        if (value != null) {
            String lowerValue = value.toLowerCase();
            if (lowerValue.length() >= NOW_LENGTH && lowerValue.startsWith(NOW)) {
                ret = true;
            } else if (lowerValue.length() >= TODAY_LENGTH && lowerValue.startsWith(TODAY)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determines value for "NOW" or "TODAY" prefixed value.
     * "NOW" format can simply be "NOW" (case insensitive), or "NOW" followed by a "+" or "=",
     * then an integer, then a unit. "M" or "MINUTE" or "MINUTES", "H" or "HOUR" or "HOURS", "D" or "DAY"
     * or "DAYS", "Y", "YEAR", or 'YEARS".
     * "TODAY" format can be simply "TODAY" (case insensitive), or "TODAY" followed by a "+" or "-",
     * then an integer, to calculate an offset from today.
     *
     * @param value   value to convert to a date
     * @param colType Column type (e.g. "timestamp", "time", "date", or "datetime")
     * @return calculated date, or null if is not a NOW or TODAY prefixed value.
     * @throws DateParseException if colType is not one of "timestamp", "time", "date", or "datetime"
     */
    //CHECKSTYLE:OFF Cyclomatic Complexity
    public static Date doNowOrToday(String value, String colType) throws DateParseException {
        if (!isNowOrTodayFormat(value)) {
            return null;
        }

        Calendar c = Calendar.getInstance();
        String lowerValue = value.toLowerCase();
        if (lowerValue.startsWith(NOW)) {
            parseNow(value, lowerValue, c);
        } else if (lowerValue.startsWith(TODAY)) {
            parseToday(value, lowerValue, c);
        } else {
            throw new DateParseException("Programmer error: " + value + " does not start with 'NOW' or 'TODAY'");
        }

        Date today = c.getTime();

        if (colType == null || colType.length() == 0) {
            throw new DateParseException("Must supply non-null column type when using 'NOW' or 'TODAY' value.");
        } else if (colType.equalsIgnoreCase("timestamp")) {
            return new java.sql.Timestamp(today.getTime());
        } else if (colType.equalsIgnoreCase("time")) {
            // A little odd using TODAY format with a TIME type column, but we'll do it - will get current time...
            return new java.sql.Time(today.getTime());
        } else if (colType.equalsIgnoreCase("date") || colType.equalsIgnoreCase("datetime")) {
            return new java.sql.Date(today.getTime());
        } else {
            throw new DateParseException("Unrecognized colType " + colType
                    + " when using 'NOW' or 'TODAY' value; expected one of date, time, datetime, or timestamp");
        }

    }
    //CHECKSTYLE:ON Cyclomatic Complexity

    /**
     * Parses "NOW" type specification, and applies offset (if any) to calendar value.
     * @param value original value
     * @param lowerValue lower cased value.
     * @param c Calendar with appropriate offset applied.
     * @throws DateParseException if anything is wrong with the NOW specification.
     */
    //CHECKSTYLE:OFF Cyclomatic Complexity
    private static void parseNow(String value, String lowerValue, Calendar c) throws DateParseException {
        if (lowerValue.length() > NOW_LENGTH) {
            Matcher matcher = NOW_OFFSET_PATTERN.matcher(lowerValue);
            if (!matcher.find()) {
                throw new DateParseException("Improper value in 'NOW' value: " + value
                        + ". 'NOW' must be followed by + or -, then numeric offset, then units (h{our{s}}, "
                        + "m{inute{s}}, d{ay{s}}, or y{ears}");
            }
            if (matcher.groupCount() != 4) {
                throw new DateParseException("Improper value in 'NOW' value: " + value + ". Pattern match returned "
                        + matcher.groupCount() + " instead of 4");
            }
            char sign = matcher.group(2).charAt(0);
            if (sign != '+' && sign != '-') {
                throw new DateParseException("Improper sign in 'NOW' value '" + lowerValue + "'");
            }
            String offsetStr = matcher.group(3);
            int offset;
            try {
                offset = Integer.parseInt(offsetStr);
            } catch (NumberFormatException e) {
                throw new DateParseException("Improper offset in 'NOW' value '" + value + "'");
            }
            if (sign == '-') {
                offset = -offset;
            }
            int calendarField;
            String units = matcher.group(4);
            if ("years".startsWith(units)) {
                calendarField = Calendar.YEAR;
            } else if ("days".startsWith(units)) {
                calendarField = Calendar.DATE;
            } else if ("hours".startsWith(units)) {
                calendarField = Calendar.HOUR;
            } else if ("minutes".startsWith(units)) {
                calendarField = Calendar.MINUTE;
            } else {
                throw new DateParseException("Improper units in 'NOW' value: '" + units + "', must be y{ear{s}}, "
                        + "d{ay{s}}, h{our{s}}, or m{inute{s}}");
            }
            c.add(calendarField, offset);
        }
    }
    //CHECKSTYLE:ON Cyclomatic Complexity

    /**
     * Parses "TODAY" type specification, and applies offset (if any) to calendar value.
     * @param value original value
     * @param lowerValue lower cased value.
     * @param c Calendar with appropriate offset applied.
     * @throws DateParseException if anything is wrong with the TODAY specification.
     */
    private static void parseToday(String value, String lowerValue, Calendar c) throws DateParseException {
        if (lowerValue.length() > TODAY_LENGTH) {
            Matcher matcher = TODAY_OFFSET_PATTERN.matcher(lowerValue);
            if (!matcher.find()) {
                throw new DateParseException("Improper value in 'TODAY' value: " + value
                        + ". 'TODAY' must be followed by + or -, then numeric offset");
            }
            if (matcher.groupCount() != 3) {
                throw new DateParseException("Improper value in 'TODAY' value: " + value + ". Pattern match returned "
                        + matcher.groupCount() + " instead of 3");
            }
            char sign = matcher.group(2).charAt(0);
            if (sign != '+' && sign != '-') {
                throw new DateParseException("Improper sign in 'TODAY' value '" + lowerValue + "'");
            }
            String offsetStr = matcher.group(3);
            int offset;
            try {
                offset = Integer.parseInt(offsetStr);
            } catch (NumberFormatException e) {
                throw new DateParseException("Improper offset in 'TODAY' value '" + value + "'");
            }
            if (sign == '-') {
                offset = -offset;
            }
            c.add(Calendar.DATE, offset);
        }
    }
}
