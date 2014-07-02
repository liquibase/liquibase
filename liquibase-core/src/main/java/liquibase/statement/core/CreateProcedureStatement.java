package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

public class CreateProcedureStatement extends AbstractStatement {

    private final static String CATALOG_NAME = "catalogName";
    private final static String SCHEMA_NAME = "schemaName";
    private final static String PROCEDURE_NAME = "procedureName";
    private final static String PROCEDURE_TEXT = "procedureText";
    private final static String END_DELIMITER = "endDelimiter";

    public CreateProcedureStatement() {
    }

    public CreateProcedureStatement(String catalogName, String schemaName, String procedureName, String procedureText, String endDelimiter) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setProcedureName(procedureName);
        setProcedureText(procedureText);
        setEndDelimiter(endDelimiter);
    }

    public String getCatalogName() {
        return getAttribute(CATALOG_NAME, String.class);
    }

    public Statement setCatalogName(String catalogName) {
        return (Statement) setAttribute(CATALOG_NAME, catalogName);
    }

    public String getSchemaName() {
        return getAttribute(SCHEMA_NAME, String.class);
    }

    public Statement setSchemaName(String schemaName) {
        return (Statement) setAttribute(SCHEMA_NAME, schemaName);
    }

    public String getProcedureName() {
        return getAttribute(PROCEDURE_NAME, String.class);
    }

    public Statement setProcedureName(String procedureName) {
        return (Statement) setAttribute(PROCEDURE_NAME, procedureName);
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
