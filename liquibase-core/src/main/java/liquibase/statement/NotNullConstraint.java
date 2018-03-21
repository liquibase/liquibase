package liquibase.statement;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;
    /**
     * Default value is true
     */
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

    public boolean shouldValidateNullable() {
        return validateNullable;
    }

    public void setValidateNullable(boolean validateNullable) {
        this.validateNullable = validateNullable;
    }
}
