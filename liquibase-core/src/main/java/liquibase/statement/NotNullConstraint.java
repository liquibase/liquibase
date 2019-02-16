package liquibase.statement;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;

    /* Some RDBMS (e.g. Oracle Database) have names for NOT NULL constraints. For Liquibase to make use of this
     * property, the Database-specific class must override supportsNotNullConstraintNames() to true.  */
    private String name;

    public NotNullConstraint() {
    }

    public NotNullConstraint(String columnName) {
        setColumnName(columnName);
    }

    public String getColumnName() {
        return columnName;
    }

    public NotNullConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getName() {
        return name;
    }

    public NotNullConstraint setName(String name) {
        this.name = name;
        return this;
    }
}
