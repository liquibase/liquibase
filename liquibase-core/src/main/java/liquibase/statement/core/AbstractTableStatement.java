package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

/**
 * Convenience base class for Statements that work on a table.
 */
abstract class AbstractTableStatement extends AbstractStatement {

    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";
    public static final String TABLE_NAME = "tableName";

    protected AbstractTableStatement() {
    }

    public AbstractTableStatement(String catalogName, String schemaName, String tableName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setTableName(tableName);
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

    public String getTableName() {
        return getAttribute(TABLE_NAME, String.class);
    }

    public Statement setTableName(String tableName) {
        return (Statement) setAttribute(TABLE_NAME, tableName);
    }
}
