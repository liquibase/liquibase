package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Drops an existing column.
 */
public class DropColumnStatement extends AbstractColumnStatement {

    public DropColumnStatement() {
    }

    public DropColumnStatement(String catalogName, String schemaName, String tableName, String columnName) {
        super(catalogName, schemaName, tableName, columnName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Column().setName(getColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
