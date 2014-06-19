package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;

public class CreateProcedureStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
    private String endDelimiter;

    public CreateProcedureStatement(String catalogName, String schemaName, String procedureName, String procedureText, String endDelimiter) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
        this.procedureText = procedureText;
        this.endDelimiter = endDelimiter;
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

    public String getProcedureText() {
        return procedureText;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
