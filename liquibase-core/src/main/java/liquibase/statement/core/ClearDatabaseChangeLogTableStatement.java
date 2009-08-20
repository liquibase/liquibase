package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class ClearDatabaseChangeLogTableStatement implements SqlStatement {

    private String schemaName;

    public ClearDatabaseChangeLogTableStatement(String schemaName) {
        super();
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
