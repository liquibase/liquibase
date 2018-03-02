package liquibase.statement;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;
    /**
     * Default value is true
     */
    private boolean validate = true;


    public NotNullConstraint() {
    }

    public NotNullConstraint(String columnName) {
        setColumnName(columnName);
    }

    public NotNullConstraint(String columnName, boolean validate) {
        setColumnName(columnName);
        setValidate(validate);
    }


    public String getColumnName() {
        return columnName;
    }

    public NotNullConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public boolean shouldValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
