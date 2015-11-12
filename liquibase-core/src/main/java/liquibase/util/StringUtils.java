package liquibase.util;

import liquibase.ExtensibleObject;
import liquibase.database.Database;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectReference;

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

        String currentString = "";
        String previousPiece = null;
        boolean previousDelimiter = false;
        for (Object piece : parsed.toArray(true)) {
            if (splitStatements && piece instanceof String && isDelimiter((String) piece, previousPiece, endDelimiter)) {
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
            previousPiece = (String) piece;
        }

        if (StringUtils.trimToNull(currentString) != null) {
            returnArray.add(currentString);
        }

        return returnArray.toArray(new String[returnArray.size()]);
    }

    protected static boolean isDelimiter(String piece, String previousPiece, String endDelimiter) {
        if (endDelimiter == null) {
            return piece.equals(";") || (piece.equalsIgnoreCase("go") && (previousPiece == null || previousPiece.endsWith("\n")));
        }

        endDelimiter = endDelimiter.replace("\\n", "").replace("\\r", "");

        return piece.toLowerCase().matches(endDelimiter.toLowerCase());
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
        return returnString.substring(0, returnString.length()-delimiter.length());
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

    public static String join(ExtensibleObject extensibleObject, String delimiter) {
        return join(extensibleObject, delimiter, new ToStringFormatter());
    }

    public static String join(ExtensibleObject extensibleObject, String delimiter, StringUtilsFormatter formatter) {
        List<String> list = new ArrayList<String>();
        for (String attribute : new TreeSet<>(extensibleObject.getAttributeNames())) {
            list.add(attribute+"="+formatter.toString(extensibleObject.get(attribute, Object.class)));
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
        return returnString.substring(0, returnString.length()-delimiter.length());
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
        return string.replace("\r\n", "\n").replace("\r","\n");
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
     * Returns the original value, unless it is null or only whitespace. If so, it returns the defaultValue.
     */
    public static String defaultIfEmpty(String original, String defaultValue) {
       if (trimToNull(original) == null) {
           return defaultValue;
       } else {
           return original;
       }
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

    public static class DefaultFormatter implements StringUtilsFormatter {
        @Override
        public String toString(Object obj) {
            if (obj == null) {
                return null;
            } else if (obj instanceof Class) {
                return ((Class) obj).getName();
            } else if (obj instanceof Object[]) {
                return "["+StringUtils.join((Object[]) obj, ", ", this)+"]";
            }
            return obj.toString();
        }
    }

    public static class ObjectNameFormatter implements StringUtilsFormatter<ObjectReference> {

        private Database database;
        private Class<? extends DatabaseObject> objectType;

        public ObjectNameFormatter(Class<? extends DatabaseObject> objectType, Database database) {
            this.objectType = objectType;
            this.database = database;

            if (this.database == null) {
                this.database = new UnsupportedDatabase();
            }
        }

        @Override
        public String toString(ObjectReference obj) {
            return database.escapeObjectName(obj);
        }
    }

    public static class ObjectSimpleNameFormatter implements StringUtilsFormatter<ObjectReference> {

        private Database database;
        private Class<? extends DatabaseObject> objectType;

        public ObjectSimpleNameFormatter(Class<? extends DatabaseObject> objectType, Database database) {
            this.objectType = objectType;
            this.database = database;

            if (this.database == null) {
                this.database = new UnsupportedDatabase();
            }
        }

        @Override
        public String toString(ObjectReference obj) {
            return database.escapeObjectName(obj.name, objectType);
        }
    }

    public static class ObjectStringNameFormatter implements StringUtilsFormatter<String> {

        private Database database;
        private Class<? extends DatabaseObject> objectType;

        public ObjectStringNameFormatter(Class<? extends DatabaseObject> objectType, Database database) {
            this.objectType = objectType;
            this.database = database;

            if (this.database == null) {
                this.database = new UnsupportedDatabase();
            }
        }

        @Override
        public String toString(String obj) {
            return database.escapeObjectName(obj, objectType);
        }
    }

    public static String limitSize(String string, int maxLength) {
        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 3) + "...";
        }
        return string;
    }

}
