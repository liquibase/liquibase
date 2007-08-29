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
     * Removes any comments from multiline SQL using {@link #stripComments(String)}
     *  and then extracts each individual statement using {@link #splitSQL(String)}.
     * 
     * @param multiLineSQL
     */
    public static String[] processMutliLineSQL(String multiLineSQL) {
        String stripped = stripComments(multiLineSQL);
        return splitSQL(stripped);
    }

    /**
     * Splits a (possible) multi-line SQL statement along ;'s and "go"'s.
     */
    public static String[] splitSQL(String multiLineSQL) {
        return multiLineSQL.split(";|\ngo\\s*\n|\ngo$");
    }
    
    /**
     * Searches through a String which contains SQL code and strips out
     * any comments that are between \/**\/ or anything that matches
     * SP--SP<text>\n (to support the ANSI standard commenting of --
     * at the end of a line).
     * 
     * @return The String without the comments in
     */
    public static String stripComments(String multiLineSQL) {
        String strippedSingleLines = multiLineSQL.replaceAll("\\s--\\s.*", "");
        String strippedMultiLines = strippedSingleLines.replaceAll("/\\*[\n\\S\\s]*\\*/", "\n");
        return strippedMultiLines;
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
