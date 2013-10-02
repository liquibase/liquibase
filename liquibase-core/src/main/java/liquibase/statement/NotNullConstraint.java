package liquibase.statement;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;


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
}
