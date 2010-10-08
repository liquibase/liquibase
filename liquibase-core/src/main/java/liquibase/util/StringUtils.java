package liquibase.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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
     * Removes any comments from multiple line SQL using {@link #stripComments(String)}
     *  and then extracts each individual statement using {@link #splitSQL(String, String)}.
     * 
     * @param multiLineSQL A String containing all the SQL statements
     * @param stripComments If true then comments will be stripped, if false then they will be left in the code
     */
    public static String[] processMutliLineSQL(String multiLineSQL,boolean stripComments, boolean splitStatements, String endDelimiter) {
        
        String stripped = stripComments ? stripComments(multiLineSQL) : multiLineSQL;
	if (splitStatements) {
	    return splitSQL(stripped, endDelimiter);
	} else {
	    return new String[]{stripped};
	}
    }

    /**
     * Splits a (possible) multi-line SQL statement along ;'s and "go"'s.
     */
    public static String[] splitSQL(String multiLineSQL, String endDelimiter) {
        if (endDelimiter == null) {
            endDelimiter = ";\\s*\n|;$|\n[gG][oO]\\s*\n|\n[Gg][oO]\\s*$";
        }
        String[] initialSplit = multiLineSQL.split(endDelimiter);
        List<String> strings = new ArrayList<String>();
        for (String anInitialSplit : initialSplit) {
            String singleLineSQL = anInitialSplit.trim();
            if (singleLineSQL.length() > 0) {
                strings.add(singleLineSQL);
            }
        }

        return strings.toArray(new String[strings.size()]);
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
        String strippedSingleLines = Pattern.compile("(.*?)\\s*\\-\\-.*\n").matcher(multiLineSQL).replaceAll("$1\n");
        strippedSingleLines = Pattern.compile("(.*?)\\s*\\-\\-.*$").matcher(strippedSingleLines).replaceAll("$1\n");
        return Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL).matcher(strippedSingleLines).replaceAll("").trim();
    }

    public static String join(String[] array, String delimiter) {
        return join(Arrays.asList(array), delimiter);
    }

    public static String join(Collection<String> collection, String delimiter) {
        if (collection == null) {
            return null;
        }

        if (collection.size() == 0) {
            return "";
        }
        
        StringBuffer buffer = new StringBuffer();
        for (String val : collection) {
            buffer.append(val).append(delimiter);
        }

        String returnString = buffer.toString();
        return returnString.substring(0, returnString.length()-delimiter.length());
    }

    public static List<String> splitAndTrim(String s, String regex) {
        if (s == null) {
            return null;
        }
        List<String> returnList = new ArrayList<String>();
        for (String string : s.split(regex)) {
            returnList.add(string.trim());
        }

        return returnList;


    }

    public static String repeat(String string, int times) {
        String returnString = "";
        for (int i=0; i<times; i++) {
            returnString += string;
        }

        return returnString;
    }
}
