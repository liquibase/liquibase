package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

/**
 * Convenience base class for Statements that work on a column.
 */
abstract class AbstractColumnStatement extends AbstractStatement {

    private final String CATALOG_NAME = "catalogName";
    private final String SCHEMA_NAME = "schemaName";
    private final String TABLE_NAME = "tableName";
    private final String COLUMN_NAME = "columnName";

    protected AbstractColumnStatement() {
    }

    public AbstractColumnStatement(String catalogName, String schemaName, String tableName, String columnName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setTableName(tableName);
        setColumnName(columnName);
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


    public String getColumnName() {
        return getAttribute(COLUMN_NAME, String.class);
    }

    public Statement setColumnName(String columnName) {
        return (Statement) setAttribute(COLUMN_NAME, columnName);
    }



}
