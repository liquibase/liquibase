package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class SelectFromDatabaseChangeLogLockStatement extends AbstractStatement {

    private String[] columnsToSelect;

    public SelectFromDatabaseChangeLogLockStatement(String... columnsToSelect) {
        this.columnsToSelect = columnsToSelect;
    }

    public String[] getColumnsToSelect() {
        return columnsToSelect;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}