package liquibase.statement;

import liquibase.AbstractExtensibleObject;

/**
 * Describes a general database function, used in {@link liquibase.statement.Statement} objects.
 * No attempts at normalization or case-fixing is made.
 */
public class DatabaseFunction extends AbstractExtensibleObject {

    private static final String TEXT = "text";

    public DatabaseFunction() {
    }

    public DatabaseFunction(String text) {
        setAttribute(TEXT, text);
    }

    /**
     * Contains the text of the database function.
     */
    public String getText() {
        return getAttribute(TEXT, String.class);
    }

    public DatabaseFunction setText(String text) {
        return (DatabaseFunction) setAttribute(TEXT, text);
    }

    @Override
    public String toString() {
        return getText();
    }

    /**
     * Two database functions are equal if their {@link #getText()} values are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatabaseFunction) {
            return this.toString().equals(obj.toString());
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }
}
