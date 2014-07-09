package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

public abstract class AbstractProcedureStatement extends AbstractStatement {
    public final static String CATALOG_NAME = "catalogName";
    public final static String SCHEMA_NAME = "schemaName";
    public final static String PROCEDURE_NAME = "procedureName";

    protected AbstractProcedureStatement() {
    }

    public AbstractProcedureStatement(String catalogName, String schemaName, String procedureName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setProcedureName(procedureName);

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
}
