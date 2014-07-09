package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class SelectFromDatabaseChangeLogLockStatement extends AbstractStatement {

    public static final String COLUMNS_TO_SELECT = "columnsToSelect";

    public SelectFromDatabaseChangeLogLockStatement(String... columnsToSelect) {
        setColumnsToSelect(columnsToSelect);
    }

    public String[] getColumnsToSelect() {
        return getAttribute(COLUMNS_TO_SELECT, String[].class);
    }

    public SelectFromDatabaseChangeLogLockStatement setColumnsToSelect(String... columnsToSelect) {
        if (columnsToSelect == null || columnsToSelect.length == 0) {
            return (SelectFromDatabaseChangeLogLockStatement) setAttribute(COLUMNS_TO_SELECT, null);
        }
        return (SelectFromDatabaseChangeLogLockStatement) setAttribute(COLUMNS_TO_SELECT, columnsToSelect);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}