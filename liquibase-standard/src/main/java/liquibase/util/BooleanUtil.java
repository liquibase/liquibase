package liquibase.util;

import org.apache.commons.lang3.BooleanUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Various utility methods for working with boolean objects.
 */
public class BooleanUtil {

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

        // Check is made to parse int as late as possible
        return trueValues.contains(value) || (!falseValues.contains(value) && isTrue(value));
    }

    private static boolean isTrue(String str) {
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Checks if a {@link Boolean} value is {@code true}, handling <b>null</b> as {@code false}.
     *  - isTrue(null)  = false
     *  - isTrue(false) = false
     *  - isTrue(true)  = true
     * @deprecated use {@link BooleanUtils#isTrue(Boolean)} instead
     */
    @Deprecated
    public static boolean isTrue(Boolean value) {
        return BooleanUtils.isTrue(value);
    }
}
