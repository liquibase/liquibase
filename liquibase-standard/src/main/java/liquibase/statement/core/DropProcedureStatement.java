package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropProcedureStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String procedureName;

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getProcedureName() {
        return procedureName;
    }
}
