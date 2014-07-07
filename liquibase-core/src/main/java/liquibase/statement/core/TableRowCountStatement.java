package liquibase.statement.core;

import liquibase.structure.DatabaseObject;

/**
 * Query the number of rows in a table.
 */
public class TableRowCountStatement extends AbstractTableStatement {

    public TableRowCountStatement() {
    }

    public TableRowCountStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
