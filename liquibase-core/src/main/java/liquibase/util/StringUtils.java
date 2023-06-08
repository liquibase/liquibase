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
     * and then extracts each individual statement using {@link #splitSQL(String, String)}.
     *
     * @param multiLineSQL  A String containing all the SQL statements
     * @param stripComments If true then comments will be stripped, if false then they will be left in the code
     */
    public static String[] processMultiLineSQL(String multiLineSQL, boolean stripComments, boolean splitStatements, String endDelimiter) {
        StringClauses parsed = SqlParser.parse(multiLineSQL, true, !stripComments);

        List<String> returnArray = new ArrayList<>();

        StringBuilder currentString = new StringBuilder();
        String previousPiece = null;
        boolean previousDelimiter = false;
        List<Object> parsedArray = Arrays.asList(parsed.toArray(true));
        int isInClause = 0;
        List<Object> tokens = mergeTokens(parsedArray, endDelimiter);
        for (int i = 0; i < tokens.size(); i++) {
            Object piece = tokens.get(i);
            String nextPiece = null;
            int nextIndex = i + 1;
            while (nextPiece == null && nextIndex < tokens.size()) {
                nextPiece = StringUtils.trimToNull(String.valueOf(tokens.get(nextIndex)));
                nextIndex++;
            }

            if (piece instanceof String && ((String) piece).equalsIgnoreCase("BEGIN")
                    && (!"transaction".equalsIgnoreCase(nextPiece) && !"trans".equalsIgnoreCase(nextPiece))
                    && (!"backup".equalsIgnoreCase(nextPiece))) {
                isInClause++;
            }
            if (piece instanceof String && ((String) piece).equalsIgnoreCase("END") && isInClause > 0
                    && (!"transaction".equalsIgnoreCase(nextPiece) && !"trans".equalsIgnoreCase(nextPiece))
                    && (!"backup".equalsIgnoreCase(nextPiece))) {
                isInClause--;
            }

            if (isInClause == 0 && splitStatements && (piece instanceof String) && isDelimiter((String) piece, previousPiece, endDelimiter)) {
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
            if (!returnArray.isEmpty() && isLastStatementComment(parsed, trimmedString)) {
                String lastStatement = returnArray.get(returnArray.size() - 1);
                String commentSeparator = getWhiteSpaceSymbolBeforeLastComment(parsed, " ");
                String lastStatementWithAppendedComment = lastStatement + commentSeparator + trimmedString;
                returnArray.set(returnArray.size() - 1, lastStatementWithAppendedComment);
            } else {
                returnArray.add(trimmedString);
            }
        }

        return returnArray.toArray(new String[0]);
    }


    /**
     * Checks if the last statement in the given string clauses is a comment and matches the provided last string statement.
     *
     * @param stringClauses is the {@link StringClauses} object containing the parsed string clauses.
     * @param lastStringStatement is the last string statement parsed out of the SQL string.
     * @return {@code true} if lastStringStatement is SQL comment (without any additional statements) and it is last clause,
     *         {@code false} otherwise.
     */
    private static boolean isLastStatementComment(StringClauses stringClauses, String lastStringStatement) {
        List<Object> parsedStringClauses = Arrays.asList(stringClauses.toArray(false));
        Optional<StringClauses.Comment> lastStringClause = getLastStringClause(parsedStringClauses);

        if (lastStringClause.isEmpty()) {
            return false;
        }

        if (!lastStringClause.get().toString().equals(lastStringStatement)) {
            return false;
        }

        return true;
    }

    /**
     * Retrieves the last {@link StringClauses} of type {@link StringClauses.Comment} from a list of parsed string clauses.
     *
     * @param parsedStringClauses is a list of parsed string clauses.
     * @return an Optional containing the last {@link StringClauses.Comment} from {@link StringClauses} if found, or an empty Optional if not found.
     */
    private static Optional<StringClauses.Comment> getLastStringClause(List<Object> parsedStringClauses) {
        for (int i = parsedStringClauses.size() - 1; i >= 0; i--) {
            if (parsedStringClauses.get(i) instanceof StringClauses.Comment) {
                return Optional.of((StringClauses.Comment) parsedStringClauses.get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves the whitespace symbol occurring immediately before the last comment in the given string clauses.
     * If no whitespace symbol is found before the last comment, the default separator is returned.
     *
     * @param stringClauses is an object containing the clauses to search for the last comment and preceding whitespace.
     * @param defaultSeparator is a default separator to return if no whitespace symbol is found before the last comment.
     * @return The whitespace symbol occurring before the last comment, or the default separator if not found.
     */
    protected static String getWhiteSpaceSymbolBeforeLastComment(StringClauses stringClauses, String defaultSeparator) {
        List<Object> parsedStringClauses = Arrays.asList(stringClauses.toArray(false));
        for (int i = parsedStringClauses.size() - 1; i >= 0; i--) {
            if (parsedStringClauses.get(i) instanceof StringClauses.Comment) {
                if (i > 0 && parsedStringClauses.get(i - 1) instanceof StringClauses.Whitespace) {
                    return parsedStringClauses.get(i - 1).toString();
                } else {
                    break;
                }
            }
        }
        return defaultSeparator;
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
                String possibleMergeString = StringUtils.join(possibleMerge, "") + obj.toString();
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

    public static String leftPad(String value, int length) {
        value = StringUtils.trimToEmpty(value);
        if (value.length() >= length) {
            return value;
        }

        return StringUtils.repeat(" ", length - value.length()) + value;
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
            if ((c == '-') && (sqlString.length() > i + 1)) {
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

}
