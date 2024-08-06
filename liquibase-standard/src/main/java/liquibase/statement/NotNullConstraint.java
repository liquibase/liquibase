package liquibase.statement;

import lombok.Getter;

public class NotNullConstraint implements ColumnConstraint {
    @Getter
    private String columnName;

    /* Some RDBMS (e.g. Oracle Database) have names for NOT NULL constraints. For Liquibase to make use of this
     * property, the Database-specific class must override supportsNotNullConstraintNames() to true.  */
    @Getter
    private String constraintName;

    /** Default value is true */
    private boolean validateNullable = true;

    public NotNullConstraint() {
    }

    public NotNullConstraint(String columnName) {
        setColumnName(columnName);
    }

    public NotNullConstraint(String columnName, boolean validateNullable) {
        setColumnName(columnName);
        setValidateNullable(validateNullable);
    }


    public NotNullConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public NotNullConstraint setConstraintName(String name) {
        this.constraintName = name;
        return this;
    }

    public boolean shouldValidateNullable() {
        return validateNullable;
    }

    public NotNullConstraint setValidateNullable(boolean validateNullable) {
        this.validateNullable = validateNullable;
        return this;
    }
}
