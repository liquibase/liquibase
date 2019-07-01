package liquibase.statement;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;
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


    public String getColumnName() {
        return columnName;
    }

    public NotNullConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public NotNullConstraint setConstraintName(String constraintName) {
        this.constraintName = constraintName;
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
