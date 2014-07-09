package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

public class CreateProcedureStatement extends AbstractProcedureStatement {

    public final static String PROCEDURE_TEXT = "procedureText";
    public final static String END_DELIMITER = "endDelimiter";

    public CreateProcedureStatement() {
    }

    public CreateProcedureStatement(String catalogName, String schemaName, String procedureName, String procedureText, String endDelimiter) {
        super(catalogName, schemaName, procedureName);
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setProcedureName(procedureName);
        setProcedureText(procedureText);
        setEndDelimiter(endDelimiter);
    }

    public String getProcedureText() {
        return getAttribute(PROCEDURE_TEXT, String.class);
    }

    public Statement setProcedureText(String procedureText) {
        return (Statement) setAttribute(PROCEDURE_TEXT, procedureText);
    }

    public String getEndDelimiter() {
        return getAttribute(END_DELIMITER, String.class);
    }

    public Statement setEndDelimiter(String endDelimiter) {
        return (Statement) setAttribute(END_DELIMITER, endDelimiter);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
