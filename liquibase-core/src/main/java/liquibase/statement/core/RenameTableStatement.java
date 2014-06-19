package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class RenameTableStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String oldTableName;
    private String newTableName;

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getNewTableName()).setSchema(getCatalogName(), getSchemaName()),
            new Table().setName(getOldTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
