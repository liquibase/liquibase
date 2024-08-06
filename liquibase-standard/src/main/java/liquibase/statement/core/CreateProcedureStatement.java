package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class CreateProcedureStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String procedureName;
    private final String procedureText;
    private final String endDelimiter;
    private Boolean replaceIfExists;

    public CreateProcedureStatement(String catalogName, String schemaName, String procedureName, String procedureText, String endDelimiter) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
        this.procedureText = procedureText;
        this.endDelimiter = endDelimiter;
    }

    public void setReplaceIfExists(Boolean replaceIfExists) {
        this.replaceIfExists = replaceIfExists;
    }
}
