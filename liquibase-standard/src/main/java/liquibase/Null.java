package liquibase;

/**
 * Represents a SQL NULL value, distinct from the Java null reference
 * and from string literals "null" or "NULL".
 * <p>
 * This class is used to disambiguate between:
 * <ul>
 *   <li>Actual SQL NULL values (represented by this class)</li>
 *   <li>String values that happen to be "null" or "NULL"</li>
 * </ul>
 */
public class Null {
    private static final Null INSTANCE = new Null();

    private Null() {
        // Private constructor for singleton pattern
    }

    public static Null getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "NULL";
    }

    @Override
    public int hashCode() {
        return Null.class.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Null;
    }
}
