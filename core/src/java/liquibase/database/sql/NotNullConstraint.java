package liquibase.database.sql;

public class NotNullConstraint implements ColumnConstraint {
    private String columnName;


    public NotNullConstraint() {
    }

    public NotNullConstraint(String columnName) {
        this.columnName = columnName;
    }


    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
}
