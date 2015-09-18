/**
 * Copyright 2015 SirsiDynix.  All rights reserved.
 */

package liquibase.util;

import liquibase.exception.DateParseException;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility code for handling date values in CSV data files that are 'TODAY'
 * literals.  (e.g. TODAY, TODAY+1 (tomorrow), TODAY-1 (yesterday).
 */
public class TodayUtil {
    private static final String TODAY = "today";
    private static final int TODAY_LENGTH = TODAY.length();

    /**
     * Checks if date starts with "TODAY".
     * @param value value to test
     * @return true if value starts with "TODAY" (case insensitive), false otherwise.
     */
    public static boolean isTodayFormat(String value) {
        if (value == null || value.length() < TODAY_LENGTH) {
            return false;
        }
        String prefix = value.substring(0,TODAY_LENGTH);
        return (TODAY.equalsIgnoreCase(prefix));
    }

    /**
     * Determines date for "TODAY" prefixed value.  Can be simply "TODAY" (case insensitive),
     * or "TODAY" followed by a "+" or "-", then an integer, to calculate an offset from today.
     * @param value value to convert to a date
     * @return calculated date, or null if is not a TODAY prefixed value.
     */
    public static Date doToday(String value) throws DateParseException {
        if (!isTodayFormat(value)) {
            return null;
        }
        int offset = 0;
        if (value.length() > TODAY_LENGTH)
        {
            char sign = value.charAt(TODAY_LENGTH);
            if (sign != '+' && sign != '-')
            {
                throw new DateParseException("Improper sign in date value '" + value + "'");
            }
            String offsetStr = value.substring(TODAY_LENGTH + 1);
            try {
                offset = Integer.parseInt(offsetStr);
            }
            catch (NumberFormatException e) {
                throw new DateParseException("Improper offset in date value '" + value + "'");
            }
            if (sign == '-') {
                offset = -offset;
            }
        }
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, offset);
        return c.getTime();
    }
}
