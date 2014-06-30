package liquibase.statement;

import liquibase.AbstractExtensibleObject;

/**
 * Describes that a column is not null, used in {@link liquibase.statement.Statement} objects.
 */
public class NotNullConstraint extends AbstractExtensibleObject implements ColumnConstraint {

    private static final String COLUMN_NAME = "columnName";

    public NotNullConstraint() {
    }

    public NotNullConstraint(String columnName) {
        setAttribute(COLUMN_NAME, columnName);
    }


    public String getColumnName() {
        return getAttribute(COLUMN_NAME, String.class);
    }

    public NotNullConstraint setColumnName(String columnName) {
        return (NotNullConstraint) setAttribute(COLUMN_NAME, columnName);
    }
}
