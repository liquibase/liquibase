package liquibase.util;

/**
 * Various utility methods for working with boolean objects.
 */
public final class BooleanUtils {

    private BooleanUtils() {
        throw new AssertionError("Utility class not designed for initialization");
    }

    /**
     * null-safe {@link Boolean} equality check
     *  - equals(null, null)   = true
     *  - equals(null, true)   = false
     *  - equals(null, false)  = false
     *  - equals(true, null)   = false
     *  - equals(false, null)  = false
     *  - equals(true, true)   = true
     *  - equals(false, false) = true
     */
    public static boolean equals(Boolean one, Boolean other) {
        if (one == null && other == null) {
            return true;
        } else if (one != null) {
            return one.equals(other);
        } else {
            return false;
        }
    }

    /**
     * Checks if a {@link Boolean} value is {@code true}, handling <b>null</b> as {@code false}.
     *  - isTrue(null)  = false
     *  - isTrue(false) = false
     *  - isTrue(true)  = true
     */
    public static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
