package liquibase.statement;

public class SelectFromDatabaseChangeLogLockStatement implements SqlStatement {

    private String columnToSelect;

    public SelectFromDatabaseChangeLogLockStatement(String columnToSelect) {
        this.columnToSelect = columnToSelect;
    }

    public String getColumnToSelect() {
        return columnToSelect;
    }
}