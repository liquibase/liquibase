package liquibase.statement;

import liquibase.ExtensibleObject;

/**
 * Common interface for all objects that describe constraints on a table, used in {@link liquibase.statement.Statement} objects.
 * For constraints on a column, use {@link liquibase.statement.ColumnConstraint}
 */
public interface Constraint extends ExtensibleObject {
}
