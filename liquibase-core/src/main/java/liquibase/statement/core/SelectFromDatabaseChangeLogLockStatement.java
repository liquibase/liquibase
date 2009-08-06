package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class SelectFromDatabaseChangeLogLockStatement implements SqlStatement {

    private String[] columnsToSelect;

    public SelectFromDatabaseChangeLogLockStatement(String... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
    }

    public String[] getColumnsToSelect() {
        return columnsToSelect;
    }
}