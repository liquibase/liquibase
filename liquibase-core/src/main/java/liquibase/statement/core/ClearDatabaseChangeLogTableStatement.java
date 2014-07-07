package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;
import liquibase.structure.DatabaseObject;

/**
 * Removes all objects from an database. DANGER!!!
 */
public class ClearDatabaseChangeLogTableStatement extends AbstractStatement {

    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";

    public ClearDatabaseChangeLogTableStatement() {
    }

    public ClearDatabaseChangeLogTableStatement(String catalogName, String schemaName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);    }

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

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
