package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SelectFromDatabaseChangeLogLockStatement extends AbstractSqlStatement {

    private String[] columnsToSelect;

    public SelectFromDatabaseChangeLogLockStatement(String... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
    }

    public String[] getColumnsToSelect() {
        return columnsToSelect;
    }
}