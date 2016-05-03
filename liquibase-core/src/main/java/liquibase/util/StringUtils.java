package liquibase.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
     * and then extracts each individual statement using {@link #splitSQL(String, String)}.
     *
     * <p>
     * Newlines and other special chars in the endDelimiter will be passed escaped (e.g. '\\' + 'n') but will be
     * processed.  There is a somewhat fragile check to determine whether a string is a regex or a normal delimiter
     * which is done by first making sure the expression can be parsed as a regex, and secondly whether the regex
     * matches itself.
     *
     * TODO: This probably should be part of the lexical analysis in SqlParser and not performed post-hoc here.  Doing
     * so makes it very clumsy and fragile.
     * </p>
     * 
     * @param multiLineSQL A String containing all the SQL statements
     * @param stripComments If true then comments will be stripped, if false then they will be left in the code
     * @param endDelimiter May be a simple delimter or a Java regular expression.  If null ";|\ngo" is assumed.
     */
    public static String[] processMutliLineSQL(String multiLineSQL, boolean stripComments, boolean splitStatements, String endDelimiter) {

        StringClauses parsed = SqlParser.parse(multiLineSQL, true, !stripComments);

        List<String> returnArray = new ArrayList<String>();

        String currentString = "";
        boolean previousDelimiter = false;

        // We do a poor mans tokenization of the end delimiter since we need to handle the case where it may contain newline or
        // spaces which will require us to match multiple tokens.
        String[] tokenizedDelimiter = null;
        Pattern regexDelimiter = null;
        int requiredLookBehind = 2; // If we are using default delimiter then we need at least tokens of look-behind

        if (endDelimiter != null) {
            endDelimiter = replaceEscapes(endDelimiter);
            try {
                regexDelimiter = Pattern.compile(endDelimiter, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                if (regexDelimiter.matcher(endDelimiter).matches()) {
                    regexDelimiter = null;
                    tokenizedDelimiter = tokenizeEndDelimiter(endDelimiter);;
                    requiredLookBehind = tokenizedDelimiter.length - 1;
                } else {
                    // We may need to keep the whole string
                    requiredLookBehind = parsed.toArray(true).length - 1;
                }
            } catch (PatternSyntaxException e) {
                tokenizedDelimiter = tokenizeEndDelimiter(endDelimiter);
                requiredLookBehind = tokenizedDelimiter.length - 1;
            }
        }

        // This stack is the amount of look-behind we need to match the delimiter.  lookBehindBuffer[0] is the
        // most recently parsed token.
        String[] lookBehindBuffer = new String[requiredLookBehind];
        for (Object piece : parsed.toArray(true)) {
            if (splitStatements && piece instanceof String && isDelimiter((String) piece, lookBehindBuffer, tokenizedDelimiter, regexDelimiter)) {
                currentString = StringUtils.trimToNull(currentString);
                if (currentString != null) {
                    returnArray.add(currentString);
                }
                currentString = "";
                previousDelimiter = true;
            } else {
                if (!previousDelimiter || StringUtils.trimToNull((String) piece) != null) { //don't include whitespace after a delimiter
                    if (!currentString.equals("") || StringUtils.trimToNull((String) piece) != null) { //don't include whitespace before the statement
                        currentString += piece;
                    }
                }
                previousDelimiter = false;
            }
            if (previousDelimiter) {
                Arrays.fill(lookBehindBuffer, null);
            } else if (requiredLookBehind > 0){
                pushLookBehind(requiredLookBehind, lookBehindBuffer, (String) piece);
            }
        }

        if (StringUtils.trimToNull(currentString) != null) {
            returnArray.add(currentString);
        }

        return returnArray.toArray(new String[returnArray.size()]);
    }

    private static void pushLookBehind(int lookBehind, String[] lookBehindBuffer, String piece) {
        System.arraycopy(lookBehindBuffer, 0, lookBehindBuffer, 1, lookBehind-1);
        lookBehindBuffer[0] = piece;
    }

    private static String[] tokenizeEndDelimiter(String endDelimiter) {
        String[] tokenizedDelimiter;
        StringTokenizer stringTokenizer = new StringTokenizer(endDelimiter, " \t\n\r\f", true);
        tokenizedDelimiter = new String[stringTokenizer.countTokens()];
        int i = 0;
        while (stringTokenizer.hasMoreElements()) {
            tokenizedDelimiter[i++] = stringTokenizer.nextToken();
        }
        return tokenizedDelimiter;
    }

    protected static boolean isDelimiter(String piece, String[] lookBehind, String[] endDelimiter, Pattern regexDelimiter) {

        if (regexDelimiter != null) {
            return isRegExDelimiter(piece, lookBehind, regexDelimiter);
        }
        if (endDelimiter == null) {
            // Assume default delimiter of ; or \ngo
            return piece.equals(";") || ((piece.equalsIgnoreCase("go") && (lookBehind[0] == null || lookBehind[0].equals("\n"))));
        }
        return isTokenizedDelimiter(piece, lookBehind, endDelimiter);
    }

    private static boolean isTokenizedDelimiter(String piece, String[] lookBehind, String[] endDelimiter) {
        if (endDelimiter.length == 1) {
            // Delimiter is a single token, just compare delimiter with current piece
            return piece.equalsIgnoreCase(endDelimiter[0]) || piece.matches(endDelimiter[0]);
        }

        // Delimiter is multiple tokens, match the current piece
        int delimOffset = endDelimiter.length - 1;
        if (!piece.equalsIgnoreCase(endDelimiter[delimOffset--])) {
            return false;
        }

        // Match remaining pieces in the look-behind buffer
        for (int i = 0; i < lookBehind.length; i++) {
            if (lookBehind[i] == null) return false;
            String c = endDelimiter[delimOffset--];
            if (!lookBehind[i].equalsIgnoreCase(c) && !lookBehind[i].matches(c)) return false;
        }
        return true;
    }

    private static boolean isRegExDelimiter(String piece, String[] lookBehind, Pattern regexDelimiter) {
        // Concatenate the tokens
        StringBuilder builder = new StringBuilder();
        for (int i=0; i < lookBehind.length && lookBehind[i] != null; i++) {
            builder.insert(0, lookBehind[i]);
        }
        builder.append(piece);
        return regexDelimiter.matcher(builder.toString()).find();
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

    // This method replaces escapes in a string with their character equivalent
    // The standard C/Java escapes including u, x and 0 are supported.  If the escape is not recognised
    // the escape is passed through unprocessed.
    private static String replaceEscapes(String s) {
        StringBuilder result = new StringBuilder(s.length());
        boolean isEscape = false;
        boolean isSpecial = false;
        int radix = 0;
        int bits = 0;
        int requiredDigits = 0;
        int maxDigits = 0;
        char special = 0;
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (isSpecial) {
                int x = Character.digit(c, radix);
                if (x != -1) {
                    requiredDigits--;
                    maxDigits--;
                    special = (char)((int)special << bits | x);
                    if (maxDigits == 0) {
                        result.append(special);
                        isSpecial = false;
                    }
                    continue;
                } else {
                    if (requiredDigits > 0) throw new IllegalArgumentException("Invalid special char escape for '" + s + "'");
                    result.append(special);
                    isSpecial = false;
                }
            }
            if (!isEscape) {
                if (c == '\\') {
                    isEscape = true;
                } else {
                    result.append(c);
                }

            } else {
                switch(c) {
                    case '\\':
                        result.append('\\');
                        break;
                    case 'n':
                        result.append('\n');
                        break;
                    case 'r':
                        result.append('\r');
                        break;
                    case 't':
                        result.append('\t');
                        break;
                    case 'b':
                        result.append('\b');
                        break;
                    case 'f':
                        result.append('\f');
                        break;
                    case '\'':
                        result.append('\'');
                        break;
                    case '"':
                        result.append('"');
                        break;
                    case 'x':
                        special = 0;
                        isSpecial = true;
                        radix = 16;
                        bits = 4;
                        requiredDigits = 1;
                        maxDigits = 2;
                        break;
                    case 'u':
                        special = 0;
                        isSpecial = true;
                        radix = 16;
                        bits = 4;
                        requiredDigits = 4;
                        maxDigits = 4;
                        break;
                    case '0':
                        special = 0;
                        isSpecial = true;
                        radix = 8;
                        bits = 3;
                        requiredDigits = 1;
                        maxDigits = 3;
                        break;
                    default:
                        // Preserve the escape
                        result.append('\\');
                        result.append(c);
                }
                isEscape = false;
            }
        }
        if (isSpecial) {
            if (requiredDigits > 0) throw new IllegalArgumentException("Invalid special char escape for '" + s + "'");
            result.append(special);
        } else if (isEscape) {
            throw new IllegalArgumentException("Unterminated escape in " + s);
        }
        return result.toString();
    }

    private static char unicode(String s, int i) {
        if (i + 5 > s.length()) {
            throw new IllegalArgumentException("Invalid unicode escape sequence in '" + s + "' at offset " + i + 1);
        }
        String hexString = s.substring(i + 1, i + 5);
        if (!hexString.matches("\\p{XDigit}{4}")) {
            throw new IllegalArgumentException("Invalid unicode escape sequence in '" + s + "' at offset " + i + 1);
        }
        return new Character((char)Integer.parseInt(hexString, 16));
    }

    private static char octal(String s, int i) {
        if (i + 5 > s.length()) {
            throw new IllegalArgumentException("Invalid unicode escape sequence in '" + s + "' at offset " + i + 1);
        }
        String hexString = s.substring(i + 1, i + 5);
        if (!hexString.matches("\\p{XDigit}{4}")) {
            throw new IllegalArgumentException("Invalid unicode escape sequence in '" + s + "' at offset " + i + 1);
        }
        return new Character((char)Integer.parseInt(hexString, 16));
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
