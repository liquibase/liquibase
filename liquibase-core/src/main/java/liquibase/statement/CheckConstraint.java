package liquibase.statement;

public class CheckConstraint implements ColumnConstraint {
    private String columnName;
    private String constraint;

    public CheckConstraint() {
    }

    public CheckConstraint(String columnName, String constraint) {
        setColumnName(columnName);
        setConstraint(constraint);
    }


    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getConstraint() {
        return constraint;
    }

    public String getColumnName() {
        return columnName;
    }

    public CheckConstraint setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }
}
