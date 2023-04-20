package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.statement.AbstractSqlStatement;

public class SelectFromDatabaseChangeLogLockStatement extends AbstractSqlStatement {

    private ColumnConfig[] columnsToSelect;

    public SelectFromDatabaseChangeLogLockStatement() {
        this.columnsToSelect = new ColumnConfig[0];
    }

    public SelectFromDatabaseChangeLogLockStatement(String... columnsToSelect) {
        this.columnsToSelect = new ColumnConfig[columnsToSelect.length];
        for (int i=0; i< columnsToSelect.length; i++) {
            this.columnsToSelect[i] = new ColumnConfig().setName(columnsToSelect[i]);
        }
    }

    public SelectFromDatabaseChangeLogLockStatement(ColumnConfig... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
    }

    public ColumnConfig[] getColumnsToSelect() {
        return columnsToSelect;
    }
}