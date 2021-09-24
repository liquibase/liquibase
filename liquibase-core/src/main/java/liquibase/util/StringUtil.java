package liquibase.util;

import liquibase.ExtensibleObject;
import liquibase.GlobalConfiguration;
import liquibase.Scope;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utility methods for working with strings.
 */
public class StringUtil {
    private static final Pattern upperCasePattern = Pattern.compile(".*[A-Z].*");
    private static final Pattern lowerCasePattern = Pattern.compile(".*[a-z].*");
    private static final Pattern spacePattern = Pattern.compile(" ");
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
     *
     * Removes any comments from multiple line SQL using {@link #stripComments(String)}
     *  and then extracts each individual statement using {@link #splitSQL(String, String)}.
     *
     * @param multiLineSQL A String containing all the SQL statements
     * @param stripComments If true then comments will be stripped, if false then they will be left in the code
     *
     */
    public static String[] processMultiLineSQL(String multiLineSQL, boolean stripComments, boolean splitStatements, String endDelimiter) {

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
     *
     * Removes any comments from multiple line SQL using {@link #stripComments(String)}
     *  and then extracts each individual statement using {@link #splitSQL(String, String)}.
     *
     * @param       multiLineSQL   A String containing all the SQL statements
     * @param       stripComments  If true then comments will be stripped, if false then they will be left in the code
     * @deprecated  The new method is {@link #processMultiLineSQL(String, boolean, boolean, String)} (String)}
     *
     */
    public static String[] processMutliLineSQL(String multiLineSQL, boolean stripComments, boolean splitStatements, String endDelimiter) {
        return processMultiLineSQL(multiLineSQL, stripComments, splitStatements, endDelimiter);
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
                if ("/".equals(endDelimiter)) {
                    if (previousPiece != null && !previousPiece.endsWith("\n")) {
                        return false;
                    }
                }
                return piece.toLowerCase().equalsIgnoreCase(endDelimiter.toLowerCase());
            } else {
                return piece.toLowerCase().matches(endDelimiter.toLowerCase()) || (previousPiece+piece).toLowerCase().matches("[\\s\n\r]*"+endDelimiter.toLowerCase());
            }
        }
    }

    /**
     *
     * Add new lines to the input string to cause output to wrap.  Optional line padding
     * can be passed in for the additional lines that are created
     *
     * @param    inputStr                The string to split and wrap
     * @param    wrapPoint               The point at which to split the lines
     * @param    extraLinePadding        Any additional spaces to add
     * @return   String                  Output string with new lines
     *
     */
    public static String wrap(final String inputStr, int wrapPoint, int extraLinePadding) {
        //
        // Just return
        //
        if (inputStr == null) {
            return null;
        }

        int inputLineLength = inputStr.length();
        int ptr = 0;
        int sizeOfMatch = -1;
        StringBuilder resultLine = new StringBuilder();
        while (ptr < inputLineLength) {
            Integer spaceToWrapAt = null;
            int min = ptr + wrapPoint + 1;
            Matcher matcher = spacePattern.matcher(inputStr.substring(ptr, Math.min(min, inputLineLength)));
            if (matcher.find()) {
                int matcherStart = matcher.start();
                if (matcherStart == 0) {
                    sizeOfMatch = matcher.end();
                    if (sizeOfMatch != 0) {
                        ptr += sizeOfMatch;
                        continue;
                    }
                    ptr += 1;
                }
                spaceToWrapAt = matcherStart + ptr;
            }

            //
            // Break because we do not have enough characters left to need to wrap
            //
            if (inputLineLength - ptr <= wrapPoint) {
                break;
            }

            //
            // Advance through all the spaces
            //
            while (matcher.find()) {
                spaceToWrapAt = matcher.start() + ptr;
            }

            if (spaceToWrapAt != null && spaceToWrapAt >= ptr) {
                resultLine.append(inputStr, ptr, spaceToWrapAt);
                resultLine.append(System.lineSeparator());
                for (int i=0; i < extraLinePadding; i++) {
                    resultLine.append(" ");
                }
                ptr = spaceToWrapAt + 1;
            } else {
                matcher = spacePattern.matcher(inputStr.substring(ptr + wrapPoint));
                if (matcher.find()) {
                    int matcherStart = matcher.start();
                    sizeOfMatch = matcher.end() - matcherStart;
                    spaceToWrapAt = matcherStart + ptr + wrapPoint;
                }

                if (sizeOfMatch == 0 && ptr != 0) {
                    ptr--;
                }
                if (spaceToWrapAt != null && spaceToWrapAt >= 0) {
                    resultLine.append(inputStr, ptr, spaceToWrapAt);
                    resultLine.append(System.lineSeparator());
                    for (int i=0; i < extraLinePadding; i++) {
                        resultLine.append(" ");
                    }
                    ptr = spaceToWrapAt + 1;
                } else {
                    resultLine.append(inputStr, ptr, inputStr.length());
                    ptr = inputLineLength;
                    sizeOfMatch = -1;
                }
            }
        }

        if (sizeOfMatch == 0 && ptr < inputLineLength) {
            ptr--;
        }

        //
        // Add the rest
        //
        resultLine.append(inputStr, ptr, inputLineLength);

        return resultLine.toString();
    }

    /**
     * Splits a candidate multi-line SQL statement along ;'s and "go"'s.
     */
    public static String[] splitSQL(String multiLineSQL, String endDelimiter) {
        return processMultiLineSQL(multiLineSQL, false, true, endDelimiter);
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
        if (array == null) {
            return null;
        }
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
        
        StringBuilder buffer = new StringBuilder();
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
        StringBuilder result = new StringBuilder(string.length() * times);
        for (int i = 0; i < times; i++) {
            result.append(string);
        }

        return result.toString();
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

        StringBuilder buffer = new StringBuilder();
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

    /**
     * Converts a camelCase string to a kabob-case one
     */
    public static String toKabobCase(String string) {
        if (string == null) {
            return null;
        }

        if (string.length() == 1) {
            return string;
        }

        StringBuilder outString = new StringBuilder();
        char[] charString = string.toCharArray();
        for (int i=0; i<charString.length; i++) {
            char letter = charString[i];
            if (i == 0) {
                outString.append(Character.toLowerCase(letter));
                continue;
            }
            if (Character.isUpperCase(letter)) {
                outString.append('-').append(Character.toLowerCase(letter));
            } else {
                outString.append(letter);
            }
        }

        return outString.toString();
    }

    /**
     * Converts a kabob-case or underscore_case string to a camel-case one
     */
    public static String toCamelCase(String string) {
        if (string == null) {
            return null;
        }

        StringBuilder outString = new StringBuilder();
        char[] charString = string.toCharArray();
        boolean uppercaseNext = false;
        for (char letter : charString) {
            if (letter == '-' || letter == '_') {
                uppercaseNext = true;
            } else {
                if (uppercaseNext) {
                    outString.append(Character.toUpperCase(letter));
                    uppercaseNext = false;
                } else {
                    outString.append(letter);
                }
            }
        }

        return outString.toString();
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
            if (lastBlockComment != null && ! lastBlockComment.isEmpty()) {
                str.setLength(str.length() - lastBlockComment.length());
                // we just modified the end of the string,
                // do another loop to check for next block or line comments
                strModified = true;
            }
            // now check for the line comments
            String lastLineComment = getLastLineComment(str.toString());
            if (lastLineComment != null && ! lastLineComment.isEmpty()) {
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

    //from https://stackoverflow.com/a/48588062/45756
    public static String escapeXml(CharSequence s) {
        StringBuilder sb = new StringBuilder();
        int len = s.length();
        for (int i=0;i<len;i++) {
            int c = s.charAt(i);
            if (c >= 0xd800 && c <= 0xdbff && i + 1 < len) {
                c = ((c-0xd7c0)<<10) | (s.charAt(++i)&0x3ff);    // UTF16 decode
            }
            if (c < 0x80) {      // ASCII range: test most common case first
                if (c < 0x20 && (c != '\t' && c != '\r' && c != '\n')) {
                    // Illegal XML character, even encoded. Skip or substitute
                    sb.append("&#xfffd;");   // Unicode replacement character
                } else {
                    switch(c) {
                        case '&':  sb.append("&amp;"); break;
                        case '>':  sb.append("&gt;"); break;
                        case '<':  sb.append("&lt;"); break;
                        // Uncomment next two if encoding for an XML attribute
//                  case '\''  sb.append("&apos;"); break;
//                  case '\"'  sb.append("&quot;"); break;
                        // Uncomment next three if you prefer, but not required
//                  case '\n'  sb.append("&#10;"); break;
//                  case '\r'  sb.append("&#13;"); break;
//                  case '\t'  sb.append("&#9;"); break;

                        default:   sb.append((char)c);
                    }
                }
            } else if ((c >= 0xd800 && c <= 0xdfff) || c == 0xfffe || c == 0xffff) {
                // Illegal XML character, even encoded. Skip or substitute
                sb.append("&#xfffd;");   // Unicode replacement character
            } else {
                sb.append("&#x");
                sb.append(Integer.toHexString(c));
                sb.append(';');
            }
        }
        return sb.toString();
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

    public static String stripEnclosingQuotes(String string) {
        if (string.length() > 1 &&
                (string.charAt(0) == '"' || string.charAt(0) == '\'') &&
                string.charAt(0) == string.charAt(string.length() - 1)) {
            return substring(string, 1, string.length() - 1);
        }
        else {
            return string;
        }
    }



    /** Check whether the value is 'null' (case insensitive) */
    public static boolean equalsWordNull(String value){
        return "NULL".equalsIgnoreCase(value);
    }

    /**
     * <p>Splits a String by Character type as returned by
     * {@code java.lang.Character.getType(char)}. Groups of contiguous
     * characters of the same type are returned as complete tokens, with the
     * following exception: if {@code camelCase} is {@code true},
     * the character of type {@code Character.UPPERCASE_LETTER}, if any,
     * immediately preceding a token of type {@code Character.LOWERCASE_LETTER}
     * will belong to the following token rather than to the preceding, if any,
     * {@code Character.UPPERCASE_LETTER} token.
     *
     * This code originated from the StringUtils class of https://github.com/apache/commons-lang
     *
     * Licensed to the Apache Software Foundation (ASF) under one or more
     * contributor license agreements.  See the NOTICE file distributed with
     * this work for additional information regarding copyright ownership.
     * The ASF licenses this file to You under the Apache License, Version 2.0
     * (the "License"); you may not use this file except in compliance with
     * the License.  You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     *
     * @param str       the String to split, may be {@code null}
     * @param camelCase whether to use so-called "camel-case" for letter types
     * @return an array of parsed Strings, {@code null} if null String input
     * @since 2.4
     */
    public static String[] splitByCharacterType(final String str, final boolean camelCase) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return new String[0];
        }
        final char[] c = str.toCharArray();
        final List<String> list = new ArrayList<>();
        int tokenStart = 0;
        int currentType = Character.getType(c[tokenStart]);
        for (int pos = tokenStart + 1; pos < c.length; pos++) {
            final int type = Character.getType(c[pos]);
            if (type == currentType) {
                continue;
            }
            if (camelCase && type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
                final int newTokenStart = pos - 1;
                if (newTokenStart != tokenStart) {
                    list.add(new String(c, tokenStart, newTokenStart - tokenStart));
                    tokenStart = newTokenStart;
                }
            } else {
                list.add(new String(c, tokenStart, pos - tokenStart));
                tokenStart = pos;
            }
            currentType = type;
        }
        list.add(new String(c, tokenStart, c.length - tokenStart));
        return list.toArray(new String[0]);
    }

    public static byte[] getBytesWithEncoding(String string) {
        String encoding = null;
        try {
            encoding = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentConfiguredValue().getValue();
            if (encoding != null) {
                return string.getBytes(encoding);
            }
        }
        catch (UnsupportedEncodingException uoe) {
            // Consume and fall through
            Scope.getCurrentScope().getLog(StringUtil.class).warning("Error using encoding " + encoding);
        }
        return string.getBytes(StandardCharsets.UTF_8);
    }
}
