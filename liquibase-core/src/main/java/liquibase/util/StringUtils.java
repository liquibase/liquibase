package liquibase.util;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Various utility methods for working with strings.
 */
public class StringUtils {
    private static final Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
    private static final Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
    private static final SecureRandom rnd = new SecureRandom();

    /**
     * Returns the trimmed (left and right) version of the input string. If null is passed, an empty string is returned.
     *
     * @param string the input string to trim
     * @return the trimmed string, or an empty string if the input was null.
     */
    public static String trimToEmpty(String string) {
        if (string == null) {
            return "";
        }
        return string.trim();
    }

    /**
     * Returns the trimmed (left and right) form of the input string. If the string is empty after trimming (or null
     * was passed in the first place), null is returned, i.e. the input string is reduced to nothing.
     * @param string the string to trim
     * @return the trimmed string or null
     */
    public static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        String returnString = string.trim();
        if (returnString.isEmpty()) {
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

        List<String> returnArray = new ArrayList<>();

        StringBuilder currentString = new StringBuilder();
        String previousPiece = null;
        boolean previousDelimiter = false;
        for (Object piece : parsed.toArray(true)) {
            if (splitStatements && (piece instanceof String) && isDelimiter((String) piece, previousPiece, endDelimiter)) {
                String trimmedString = StringUtils.trimToNull(currentString.toString());
                if (trimmedString != null) {
                    returnArray.add(trimmedString);
                }
                currentString = new StringBuilder();
                previousDelimiter = true;
            } else {
                if (!previousDelimiter || (StringUtils.trimToNull((String) piece) != null)) { //don't include whitespace after a delimiter
                    if ((currentString.length() > 0) || (StringUtils.trimToNull((String) piece) != null)) { //don't include whitespace before the statement
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

    /**
     * Returns true if the input is a delimiter in one of the popular RDBMSs. Recognized delimiters are: semicolon (;),
     * a slash (as the only content) or the word GO (as the only content).
     * @param piece the input line to test
     * @param previousPiece the characters in the input stream that came before piece
     * @param endDelimiter ??? (need to see this in a debugger to find out)
     * @return
     */
    protected static boolean isDelimiter(String piece, String previousPiece, String endDelimiter) {
        if (endDelimiter == null) {
            return ";".equals(piece) || (("go".equalsIgnoreCase(piece) || "/".equals(piece)) && ((previousPiece ==
                null) || previousPiece.endsWith("\n")));
        } else {
            if (endDelimiter.length() == 1) {
                return piece.toLowerCase().equalsIgnoreCase(endDelimiter.toLowerCase());
            } else {
                return piece.toLowerCase().matches(endDelimiter.toLowerCase()) || (previousPiece+piece).toLowerCase().matches("[\\s\n\r]*"+endDelimiter.toLowerCase());
            }
        }
    }

    /**
     * Splits a candidate multi-line SQL statement along ;'s and "go"'s.
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

        if (collection.isEmpty()) {
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
            TreeSet<String> sortedSet = new TreeSet<>();
            for (Object obj : collection) {
                sortedSet.add(formatter.toString(obj));
            }
            return join(sortedSet, delimiter);
        }
        return join(collection, delimiter, formatter);
    }

    public static String join(Collection<String> collection, String delimiter, boolean sorted) {
        if (sorted) {
            return join(new TreeSet<>(collection), delimiter);
        } else {
            return join(collection, delimiter);
        }
    }

    public static String join(Map map, String delimiter) {
        return join(map, delimiter, new ToStringFormatter());
    }

    public static String join(Map map, String delimiter, StringUtilsFormatter formatter) {
        List<String> list = new ArrayList<>();
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            list.add(entry.getKey().toString()+"="+formatter.toString(entry.getValue()));
        }
        return join(list, delimiter);
    }

    public static List<String> splitAndTrim(String s, String regex) {
        if (s == null) {
            return null;
        }
        List<String> returnList = new ArrayList<>();
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

    /**
     * Returns true if ch is a "7-bit-clean" ASCII character (ordinal number < 128).
     * @param ch the character to test
     * @return true if 7 bit-clean, false otherwise.
     */
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

    /**
     * Adds spaces to the right of the input value until the string has reached the given length. Nothing is done
     * if the string already has the given length or if the string is even longer.
     * @param value The string to pad (if necessary)
     * @param length the desired length
     * @return the input string, padded if necessary.
     */
    public static String pad(String value, int length) {
        value = StringUtils.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return value + StringUtils.repeat(" ", length - value.length());
    }

    /**
     * Adds spaces to the left of the input value until the string has reached the given length. Nothing is done
     * if the string already has the given length or if the string is even longer.
     * @param value The string to pad (if necessary)
     * @param length the desired length
     * @return the input string, padded if necessary.
     */
    public static String leftPad(String value, int length) {
        value = StringUtils.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return StringUtils.repeat(" ", length - value.length()) + value;
    }

    /**
     * Returns true if the input string is the empty string (null-safe).
     *
     * @param value String to be checked
     * @return true if String is null or empty
     */
    public static boolean isEmpty(String value) {
        return (value == null) || value.isEmpty();
    }

    /**
     * Returns true if the input string is NOT the empty string. If the string is null, false is returned.
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
        if((value == null) || (startsWith == null)){
            return false;
        }

        return value.startsWith(startsWith);
    }

    /**
     * Returns true if the given string only consists of whitespace characters (null-safe)
     * @param string the string to test
     * @return true if the string is null or only consists of whitespaces.
     */
    public static boolean isWhitespace(CharSequence string) {
        if (string == null) {
            return true;
        }
        return StringUtils.trimToNull(string.toString()) == null;
    }

    /**
     * Compares a minimum version number given in string form (only the first three parts are considered) with a
     * candidate version given as the three ints major, minor and patch.
     *
     * @param minimumVersion The minimum version that is required, given as a string with up to 3 parts, e.g. "7.4" or "9.6.3"
     * @param candidateMajor the version number to be tested, major part
     * @param candidateMinor the version number to be tested, minor part
     * @param candidatePatch the version number to be tested, patch part
     * @return true if candidateMajor.candidateMinor.candidatePatch >= minimumVersion or false if not
     */
    public static boolean isMinimumVersion(String minimumVersion, int candidateMajor, int candidateMinor,
                                           int candidatePatch) {
        String[] parts = minimumVersion.split("\\.", 3);
        int minMajor = Integer.parseInt(parts[0]);
        int minMinor = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0;
        int minPatch = (parts.length > 2) ? Integer.parseInt(parts[2]) : 0;

        if (minMajor > candidateMajor) {
            return false;
        }

        if ((minMajor == candidateMajor) && (minMinor > candidateMinor)) {
            return false;
        }

        return !((minMajor == candidateMajor) && (minMinor == candidateMinor) && (minPatch > candidatePatch));
    }

    public static String limitSize(String string, int maxLength) {
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }

    /**
     * Produce a random identifer of the given length, consisting only of uppercase letters.
     * @param len desired length of the string
     * @return an identifier of the desired length
     */
    public static String randomIdentifer(int len) {
        final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        StringBuilder sb = new StringBuilder( len );
        for (int i = 0; i < len; i++)
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    public interface StringUtilsFormatter<Type> {
        String toString(Type obj);
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
}
