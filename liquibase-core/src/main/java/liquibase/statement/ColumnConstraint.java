package liquibase.statement;

/**
 * Common interface for all objects that describe constraints on a column, used in {@link liquibase.statement.Statement} objects.
 */
public interface ColumnConstraint extends Constraint {

    String getColumnName();

    ColumnConstraint setColumnName(String columnName);
}
