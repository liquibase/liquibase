package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

/**
 * Reindexes and/or defragments a table.
 */
public class ReindexStatement extends AbstractTableStatement {

    public ReindexStatement() {
    }

    public ReindexStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[]{new Table(getCatalogName(), getSchemaName(), getTableName())};
    }
}
