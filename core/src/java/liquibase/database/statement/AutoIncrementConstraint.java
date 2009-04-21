package liquibase.database.statement;

public class AutoIncrementConstraint implements ColumnConstraint {
    private String columnName;

    public AutoIncrementConstraint() {
    }

    public AutoIncrementConstraint(String columnName) {
        setColumnName(columnName);
    }


    public String getColumnName() {
        return columnName;
    }

    public AutoIncrementConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

}
