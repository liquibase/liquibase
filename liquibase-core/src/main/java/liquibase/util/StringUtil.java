package liquibase.util;

import liquibase.ExtensibleObject;
import liquibase.database.Database;
import liquibase.structure.DatabaseObject;

import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Various utility methods for working with strings.
 */
public class StringUtil {
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
        List<Object> parsedArray = Arrays.asList(parsed.toArray(true));
        for (Object piece : mergeTokens(parsedArray, endDelimiter)) {
            if (splitStatements && (piece instanceof String) && isDelimiter((String) piece, previousPiece, endDelimiter)) {
                String trimmedString = StringUtil.trimToNull(currentString.toString());
                if (trimmedString != null) {
                    returnArray.add(trimmedString);
                }
                currentString = new StringBuilder();
                previousDelimiter = true;
            } else {
                if (!previousDelimiter || (StringUtil.trimToNull((String) piece) != null)) { //don't include whitespace after a delimiter
                    if ((currentString.length() > 0) || (StringUtil.trimToNull((String) piece) != null)) { //don't include whitespace before the statement
                        currentString.append(piece);
                    }
                }
                previousDelimiter = false;
            }
            previousPiece = (String) piece;
        }

        String trimmedString = StringUtil.trimToNull(currentString.toString());
        if (trimmedString != null) {
            returnArray.add(trimmedString);
        }

        return returnArray.toArray(new String[returnArray.size()]);
    }

    /**
     * Delimiters like "//" may span multiple tokens. Look for them and combine them
     */
    private static List<Object> mergeTokens(List<Object> parsedArray, String endDelimiter) {
        if (endDelimiter == null) {
            return parsedArray;
        }

        List<Object> returnList = new ArrayList<>();
        List<String> possibleMerge = new ArrayList<>();
        for (Object obj : parsedArray) {
            if (possibleMerge.size() == 0) {
                if ((obj instanceof String) && endDelimiter.startsWith((String) obj)) {
                    possibleMerge.add((String) obj);
                } else {
                    returnList.add(obj);
                }
            } else {
                String possibleMergeString = StringUtil.join(possibleMerge, "") + obj.toString();
                if (endDelimiter.equals(possibleMergeString)) {
                    returnList.add(possibleMergeString);
                    possibleMerge.clear();
                } else if (endDelimiter.startsWith(possibleMergeString)) {
                    possibleMerge.add(obj.toString());
                } else {
                    returnList.addAll(possibleMerge);
                    returnList.add(obj);
                    possibleMerge.clear();
                }
            }
        }

        return returnList;

    }

    /**
     * Returns true if the input is a delimiter in one of the popular RDBMSs. Recognized delimiters are: semicolon (;),
     * a slash (as the only content) or the word GO (as the only content).
     * @param piece the input line to test
     * @param previousPiece the characters in the input stream that came before piece
     * @param endDelimiter ??? (need to see this in a debugger to find out)
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

    public static String join(Object[] array, String delimiter, StringUtilFormatter formatter) {
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

    public static String join(Collection collection, String delimiter, StringUtilFormatter formatter) {
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

    public static String join(Collection collection, String delimiter, StringUtilFormatter formatter, boolean sorted) {
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

    public static String join(Map map, String delimiter, StringUtilFormatter formatter) {
        List<String> list = new ArrayList<>();
        for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
            list.add(entry.getKey().toString()+"="+formatter.toString(entry.getValue()));
        }
        return join(list, delimiter);
    }

   public static String join(ExtensibleObject extensibleObject, String delimiter) {
        return join(extensibleObject, delimiter, new ToStringFormatter());
    }

    public static String join(ExtensibleObject extensibleObject, String delimiter, StringUtilFormatter formatter) {
        List<String> list = new ArrayList<>();
        for (String attribute : new TreeSet<>(extensibleObject.getAttributes())) {
            String formattedValue = formatter.toString(extensibleObject.get(attribute, Object.class));
            if (formattedValue != null) {
                list.add(attribute + "=" + formattedValue);
            }
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
	return StringUtil.join(ints, delimiter);
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
        String pad = StringUtil.repeat(" ", padding);
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
        value = StringUtil.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return value + StringUtil.repeat(" ", length - value.length());
    }

    /**
     * Adds spaces to the left of the input value until the string has reached the given length. Nothing is done
     * if the string already has the given length or if the string is even longer.
     * @param value The string to pad (if necessary)
     * @param length the desired length
     * @return the input string, padded if necessary.
     */
    public static String leftPad(String value, int length) {
        value = StringUtil.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return StringUtil.repeat(" ", length - value.length()) + value;
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
        return StringUtil.trimToNull(string.toString()) == null;
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

    public interface StringUtilFormatter<Type> {
        String toString(Type obj);
    }

    public static class ToStringFormatter implements StringUtilFormatter {
        @Override
        public String toString(Object obj) {
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }

  public static class DefaultFormatter implements StringUtilFormatter {
        @Override
        public String toString(Object obj) {
            if (obj == null) {
                return null;
            } else if (obj instanceof Class) {
                return ((Class) obj).getName();
            } else if (obj instanceof Object[]) {
                if (((Object[]) obj).length == 0) {
                    return null;
                } else {
                    return "[" + StringUtil.join((Object[]) obj, ", ", this) + "]";
                }
            } else if (obj instanceof Collection) {
                if (((Collection) obj).size() == 0) {
                    return null;
                } else {
                    return "[" + StringUtil.join((Collection) obj, ", ", this) + "]";
                }

            }
            return obj.toString();
        }
    }

    /**
     * Returns if two strings are equal, ignoring:
     * <ul>
     * <li>case (uppercase/lowercase)</li>
     * <li>difference between null, and empty string, and a string that only has spaces</li>
     * </ul>
     *
     * @param s1 the first String to compare (or null)
     * @param s2 the second String to compare (or null)
     * @return true if the Strings are equal by the above criteria, false in all other cases
     */
    public static boolean equalsIgnoreCaseAndEmpty(String s1, String s2) {
        String clean1 = trimToNull(s1);
        String clean2 = trimToNull(s2);
        if (clean1 == null && clean2 == null) {
            return true;
        } else {
            // Both cannot be null at this point
            if (clean1 == null || clean2 == null) {
                return false;
            }
        }
        return clean1.equalsIgnoreCase(clean2);
    }

    /**
     * Trims {@link Character#isWhitespace(char) whitespace} characters from the
     * end of specified <code>string</code>
     * @param string String to trim
     * @return new String without the whitespace at the end
     */
    public static String trimRight(String string) {
        int i = string.length()-1;
        while (i >= 0 && Character.isWhitespace(string.charAt(i))) {
            i--;
        }
        return string.substring(0,i+1);
    }

    /**
     *
     * @param sqlString
     * @return the last block comment from a Sql string if any
     */
    public static String getLastBlockComment(String sqlString) {
        if (isEmpty(sqlString) || sqlString.length() < 4) {
            return null;
        }
        StringBuilder reversedSqlStringBuilder = new StringBuilder(sqlString).reverse();
        String reversedString = reversedSqlStringBuilder.toString();
        int idxClosingLastChar = -1, idxOpeningFirstChar = -1;
        for (int i = 0; i < reversedString.length(); i++) {
            if (idxClosingLastChar < 0) {
                // we have not found the start of the pair (reversed) yet)
                char c = reversedString.charAt(i);
                if (c == '/') {
                    // check the second one
                    char s = reversedString.charAt(i + 1);
                    if (s == '*') {
                        idxClosingLastChar = i;
                    }
                } else if (!Character.isWhitespace(c)){
                    // does not look like it ends with block comment, return null
                    return null;
                }
            } else {
                // look for closing pair (reversed)
                char c = reversedString.charAt(i);
                if (c == '/') {
                    // check the previous one
                    char s = reversedString.charAt(i - 1);
                    char e = reversedString.charAt(i + 1);
                    // if it was not escaped
                    if (s == '*' && e != '\\') {
                        idxOpeningFirstChar = i;
                        break;
                    }
                }
            }
        }

        // reverse the index to get the start of the last comment block
        int idxOfLastBlockComment = sqlString.length() - (idxOpeningFirstChar + 1);

        return sqlString.substring(idxOfLastBlockComment);
    }

    /**
     *
     * @param sqlString
     * @return the last line comment from a Sql string if any
     */
    public static String getLastLineComment(String sqlString) {
        if (isEmpty(sqlString) || sqlString.length() < 2) {
            return null;
        }
        boolean startOfNewLine = false;
        int idxOfDoubleDash = -1;
        for (int i = 0; i < sqlString.length(); i++) {
            char c = sqlString.charAt(i);
            // we have not found the start of the line comment yet
            if (c == '-') {
                // check the next one
                char s = sqlString.charAt(i + 1);
                if (s == '-') {
                    if (idxOfDoubleDash < 0) {
                        idxOfDoubleDash = i;
                    }
                    startOfNewLine = false;
                }
            } else if (!Character.isWhitespace(c)) {
                if (startOfNewLine) {
                    // new line started and we found some other character, reset the index,
                    idxOfDoubleDash = -1;
                }
            } else if (c == '\r' || c == '\n') {
                // new line found
                startOfNewLine = true;
            }

        }
        if (idxOfDoubleDash < 0) {
            return null;
        }
        return sqlString.substring(idxOfDoubleDash);
    }

    /**
     * Strips the comments and whitespaces from the end of given sql string.
     * @param sqlString
     * @return
     */
    public static String stripSqlCommentsAndWhitespacesFromTheEnd(String sqlString) {
        if (isEmpty(sqlString)) {
            return sqlString;
        }
        StringBuilder str = new StringBuilder(sqlString);
        boolean strModified = true;
        while (strModified) {
            strModified = false;
            // first check for last block comment
            // since line comments could be inside block comments, we want to
            // remove them first.
            String lastBlockComment = getLastBlockComment(str.toString());
            if (isNotEmpty(lastBlockComment)) {
                str.setLength(str.length() - lastBlockComment.length());
                // we just modified the end of the string,
                // do another loop to check for next block or line comments
                strModified = true;
            }
            // now check for the line comments
            String lastLineComment = getLastLineComment(str.toString());
            if (isNotEmpty(lastLineComment)) {
                str.setLength(str.length() - lastLineComment.length());
                // we just modified the end of the string,
                // do another loop to check for next block or line comments
                strModified = true;
            }
        }
        return trimRight(str.toString());
    }

    
    /**
     * From commonslang3 -> StringUtil
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     *
     * <p>A negative start position can be used to start/end {@code n}
     * characters from the end of the String.</p>
     *
     * <p>The returned substring starts with the character in the {@code start}
     * position and ends before the {@code end} position. All position counting is
     * zero-based -- i.e., to start at the beginning of the string use
     * {@code start = 0}. Negative start and end positions can be used to
     * specify offsets relative to the end of the String.</p>
     *
     * <p>If {@code start} is not strictly to the left of {@code end}, ""
     * is returned.</p>
     *
     * <pre>
     * StringUtil.substring(null, *, *)    = null
     * StringUtil.substring("", * ,  *)    = "";
     * StringUtil.substring("abc", 0, 2)   = "ab"
     * StringUtil.substring("abc", 2, 0)   = ""
     * StringUtil.substring("abc", 2, 4)   = "c"
     * StringUtil.substring("abc", 4, 6)   = ""
     * StringUtil.substring("abc", 2, 2)   = ""
     * StringUtil.substring("abc", -2, -1) = "b"
     * StringUtil.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str  the String to get the substring from, may be null
     * @param start  the position to start from, negative means
     *  count back from the end of the String by this many characters
     * @param end  the position to end at (exclusive), negative means
     *  count back from the end of the String by this many characters
     * @return substring from start position to end position,
     *  {@code null} if null String input
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * Concatenates the addition string to the baseString string, adjusting the case of "addition" to match the base string.
     * If the string is all caps, append addition in all caps. If all lower case, append in all lower case. If baseString is mixed case, make no changes to addition.
     */
    public static String concatConsistentCase(String baseString, String addition) {
        boolean hasLowerCase = hasLowerCase(baseString);
        boolean hasUpperCase = hasUpperCase(baseString);
        if ((hasLowerCase && hasUpperCase) || (!hasLowerCase && !hasUpperCase)) { //mixed case || no letters
            return baseString + addition;
        } else if (hasLowerCase) {
            return baseString + addition.toLowerCase();
        } else {
            return baseString + addition.toUpperCase();
        }
    }


}
