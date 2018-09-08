package liquibase.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BooleanParser {

    private final static Set<String> trueValues = new HashSet<>(Arrays.asList("true", "t", "yes", "y", "1"));
    private final static Set<String> falseValues = new HashSet<>(Arrays.asList("false", "f", "no", "n", "0"));

    /**
     * @param booleanStr not trimmed string
     * @return true, if represents values "true", "t", "yes", "y", or integer >= 1, false otherwise
     */
    public static boolean parseBoolean(String booleanStr) {
        if (booleanStr == null) {
            return false;
        }
        String value = booleanStr.trim().toLowerCase();

        // Check is made to parse int as later as possible
        return trueValues.contains(value) || (!falseValues.contains(value) && isTrue(value));
    }

    private static boolean isTrue(String str) {
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
