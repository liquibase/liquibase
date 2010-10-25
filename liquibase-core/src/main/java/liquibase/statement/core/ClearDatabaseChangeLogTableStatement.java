package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ClearDatabaseChangeLogTableStatement extends AbstractSqlStatement {

    private String schemaName;

    public ClearDatabaseChangeLogTableStatement(String schemaName) {
        super();
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
