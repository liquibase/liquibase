package liquibase.util;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Various utility methods for working with strings.
 */
public class StringUtils {
    private static final Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
    private static final Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");


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
    public static String[] processMutliLineSQL(String multiLineSQL, boolean stripComments, boolean splitStatements, String endDelimiter) {

        StringClauses parsed = SqlParser.parse(multiLineSQL, true, !stripComments);

        List<String> returnArray = new ArrayList<String>();

        StringBuilder currentString = new StringBuilder();
        String previousPiece = null;
        boolean previousDelimiter = false;
        for (Object piece : parsed.toArray(true)) {
            if (splitStatements && piece instanceof String && isDelimiter((String) piece, previousPiece, endDelimiter)) {
                String trimmedString = StringUtils.trimToNull(currentString.toString());
                if (trimmedString != null) {
                    returnArray.add(trimmedString);
                }
                currentString = new StringBuilder();
                previousDelimiter = true;
            } else {
                if (!previousDelimiter || StringUtils.trimToNull((String) piece) != null) { //don't include whitespace after a delimiter
                    if (!currentString.toString().equals("") || StringUtils.trimToNull((String) piece) != null) { //don't include whitespace before the statement
                        currentString.append(piece);
                    }
                }
                previousDelimiter = false;
            }
            previousPiece = (String) piece;
        }

        String trimmedString = StringUtils.trimToNull(currentString.toString());
        if (trimmedString != null) {
            returnArray.add(trimmedString);
        }

        return returnArray.toArray(new String[returnArray.size()]);
    }

    protected static boolean isDelimiter(String piece, String previousPiece, String endDelimiter) {
        if (endDelimiter == null) {
            return piece.equals(";") || ((piece.equalsIgnoreCase("go") || piece.equals("/")) && (previousPiece == null || previousPiece.endsWith("\n")));
        } else {
            if (endDelimiter.length() == 1) {
                return piece.toLowerCase().equalsIgnoreCase(endDelimiter.toLowerCase());
            } else {
                return piece.toLowerCase().matches(endDelimiter.toLowerCase()) || (previousPiece+piece).toLowerCase().matches("[.\n\r]*"+endDelimiter.toLowerCase());
            }
        }
    }

    /**
     * Splits a (possible) multi-line SQL statement along ;'s and "go"'s.
     */
    public static String[] splitSQL(String multiLineSQL, String endDelimiter) {
        return processMutliLineSQL(multiLineSQL, false, true, endDelimiter);
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
        return SqlParser.parse(multiLineSQL, true, false).toString().trim();
    }

    public static String join(Object[] array, String delimiter, StringUtilsFormatter formatter) {
        if (array == null) {
            return null;
        }
        return join(Arrays.asList(array), delimiter, formatter);
    }

    public static String join(String[] array, String delimiter) {
        return join(Arrays.asList(array), delimiter);
    }

    public static String join(Collection<String> collection, String delimiter) {
        return join(collection, delimiter, new ToStringFormatter());

    }

    public static String join(Collection collection, String delimiter, StringUtilsFormatter formatter) {
        if (collection == null) {
            return null;
        }

        if (collection.size() == 0) {
            return "";
        }
        
        StringBuffer buffer = new StringBuffer();
        for (Object val : collection) {
            buffer.append(formatter.toString(val)).append(delimiter);
        }

        String returnString = buffer.toString();
        return returnString.substring(0, returnString.length() - delimiter.length());
    }

    public static String join(Collection collection, String delimiter, StringUtilsFormatter formatter, boolean sorted) {
        if (sorted) {
            TreeSet<String> sortedSet = new TreeSet<String>();
            for (Object obj : collection) {
                sortedSet.add(formatter.toString(obj));
            }
            return join(sortedSet, delimiter);
        }
        return join(collection, delimiter, formatter);
    }

    public static String join(Collection<String> collection, String delimiter, boolean sorted) {
        if (sorted) {
            return join(new TreeSet<String>(collection), delimiter);
        } else {
            return join(collection, delimiter);
        }
    }

    public static String join(Map map, String delimiter) {
        return join(map, delimiter, new ToStringFormatter());
    }

    public static String join(Map map, String delimiter, StringUtilsFormatter formatter) {
        List<String> list = new ArrayList<String>();
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            list.add(entry.getKey().toString()+"="+formatter.toString(entry.getValue()));
        }
        return join(list, delimiter);
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

    public static String join(Integer[] array, String delimiter) {
        if (array == null) {
            return null;
        }

        int[] ints = new int[array.length];
        for (int i=0; i < ints.length; i++)
        {
            ints[i] = array[i];
        }
	return StringUtils.join(ints, delimiter);
    }

    public static String join(int[] array, String delimiter) {
        if (array == null) {
            return null;
        }

        if (array.length == 0) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        for (int val : array) {
            buffer.append(val).append(delimiter);
        }

        String returnString = buffer.toString();
        return returnString.substring(0, returnString.length() - delimiter.length());
    }

    public static String indent(String string) {
        return indent(string, 4);
    }

    public static String indent(String string, int padding) {
        String pad = StringUtils.repeat(" ", padding);
        return pad+(string.replaceAll("\n", "\n" + pad));
    }

    public static String lowerCaseFirst(String string) {
        return string.substring(0, 1).toLowerCase()+string.substring(1);
    }

    public static String upperCaseFirst(String string) {
        return string.substring(0, 1).toUpperCase()+string.substring(1);
    }

    public static boolean hasUpperCase(String string) {
        return upperCasePattern.matcher(string).matches();
    }

    public static boolean hasLowerCase(String string) {
        return lowerCasePattern.matcher(string).matches();
    }

    public static String standardizeLineEndings(String string) {
        if (string == null) {
            return null;
        }
        return string.replace("\r\n", "\n").replace("\r", "\n");
    }

    public static boolean isAscii(String string) {
        if (string == null) {
            return true;
        }
        for (char c : string.toCharArray()) {
            if (!isAscii(c)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAscii(char ch) {
        return ch < 128;
    }

    public static String escapeHtml(String str) {
        StringBuilder out = new StringBuilder();
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
                if (c > 0x7F) {
                    out.append("&#");
                    out.append(Integer.toString(c, 10));
                    out.append(';');
                } else {
                    out.append(c);
                }
        }
        return out.toString();
    }

    public static String pad(String value, int length) {
        value = StringUtils.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return value + StringUtils.repeat(" ", length - value.length());
    }

    /**
     * Null-safe check if string is empty.
     *
     * @param value String to be checked
     * @return true if String is null or empty
     */
    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    /**
     * Null-safe check if string is not empty
     *
     * @param value String to be checked
     * @return true if string is not null and not empty (length > 0)
     */
    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    /**
     * Checks if <code>value</code> starts with <code>startsWith</code>.
     * @param value
     * @param startsWith
     * @return true if <code>value</code> starts with <code>startsWith</code>, otherwise false. If any of arguments is null returns false
     */
    public static boolean startsWith(String value, String startsWith) {
        if(value == null || startsWith == null){
            return false;
        }

        return value.startsWith(startsWith);
    }

    public static boolean isWhitespace(CharSequence string) {
        if (string == null) {
            return true;
        }
        return StringUtils.trimToNull(string.toString()) == null;
    }

    public static interface StringUtilsFormatter<Type> {
        public String toString(Type obj);
    }

    public static class ToStringFormatter implements StringUtilsFormatter {
        @Override
        public String toString(Object obj) {
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }

    public static String limitSize(String string, int maxLength) {
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }

}
