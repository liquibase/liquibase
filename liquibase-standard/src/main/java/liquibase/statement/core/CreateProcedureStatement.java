package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateProcedureStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String procedureName;
    private String procedureText;
    private String endDelimiter;
    private Boolean replaceIfExists;

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

    public Boolean getReplaceIfExists() {
        return replaceIfExists;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }
}
