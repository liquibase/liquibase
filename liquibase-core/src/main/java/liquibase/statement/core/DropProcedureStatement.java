package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropProcedureStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String dropName;
    private String procedureArguments;

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName, String dropName, String procedureArguments) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
        this.dropName = dropName;
        this.procedureArguments = procedureArguments;
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

    public String getDropName() {
        return dropName;
    }

    public void setDropName(String dropName) {
        this.dropName = dropName;
    }

    public String getProcedureArguments() {
        return procedureArguments;
    }

    public void setProcedureArguments(String procedureArguments) {
        this.procedureArguments = procedureArguments;
    }
}
