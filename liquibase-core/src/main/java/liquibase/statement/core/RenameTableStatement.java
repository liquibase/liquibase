package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class RenameTableStatement extends AbstractSqlStatement {
    private final String catalogName;
    private final String schemaName;
    private final String oldTableName;
    private final String newTableName;

    public RenameTableStatement(String catalogName, String schemaName, String oldTableName, String newTableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.oldTableName = oldTableName;
        this.newTableName = newTableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getOldTableName() {
        return oldTableName;
    }

    public String getNewTableName() {
        return newTableName;
    }
}
