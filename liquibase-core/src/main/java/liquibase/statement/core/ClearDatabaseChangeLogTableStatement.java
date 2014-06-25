package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

public class ClearDatabaseChangeLogTableStatement extends AbstractStatement {

    private String catalogName;
    private String schemaName;

    public ClearDatabaseChangeLogTableStatement(String catalogName, String schemaName) {
        super();
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
