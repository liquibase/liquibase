package liquibase.util;

import java.util.Collection;

/**
 * Various utility methods for working with strings.
 */
public class StringUtils {
    public static String trimToEmpty(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    public static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        String returnString = string.trim();
        if (returnString.length() == 0) {
            return null;
        } else {
            return returnString;
        }
    }

    /**
     * Splits a (possible) multi-line SQL statement along ;'s and "go"'s.
     */
    public static String[] splitSQL(String multiLineSQL) {
        return multiLineSQL.split(";|\ngo\\s*\n|\ngo$");
    }

    public static String join(Collection<String> collection, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        for (String val : collection) {
            buffer.append(val).append(delimiter);
        }

        String returnString = buffer.toString();
        return returnString.substring(0, returnString.length()-delimiter.length());
    }
}
