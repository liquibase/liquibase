package liquibase.statement.core;

import liquibase.statement.AbstractStatement;

public abstract class AbstractIndexStatement extends AbstractStatement {
    public static final String TABLE_CATALOG_NAME = "tableCatalogName";
    public static final String TABLE_SCHEMA_NAME = "tableSchemaName";
    public static final String INDEX_NAME = "indexName";
    public static final String TABLE_NAME = "tableName";

    protected AbstractIndexStatement() {
    }

    public AbstractIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName) {
        setIndexName(indexName);
        setTableCatalogName(tableCatalogName);
        setTableSchemaName(tableSchemaName);
        setTableName(tableName);
    }

    public String getTableCatalogName() {
        return getAttribute(TABLE_CATALOG_NAME, String.class);
    }

    public AbstractIndexStatement setTableCatalogName(String tableCatalogName) {
        return (AbstractIndexStatement) setAttribute(TABLE_CATALOG_NAME, tableCatalogName);
    }

    public String getTableSchemaName() {
        return getAttribute(TABLE_SCHEMA_NAME, String.class);
    }

    public AbstractIndexStatement setTableSchemaName(String tableSchemaName) {
        return (AbstractIndexStatement) setAttribute(TABLE_SCHEMA_NAME, tableSchemaName);
    }

    public String getIndexName() {
        return getAttribute(INDEX_NAME, String.class);
    }

    public AbstractIndexStatement setIndexName(String indexName) {
        return (AbstractIndexStatement) setAttribute(INDEX_NAME, indexName);
    }

    public String getTableName() {
        return getAttribute(TABLE_NAME, String.class);
    }

    public AbstractIndexStatement setTableName(String tableName) {
        return (AbstractIndexStatement) setAttribute(TABLE_NAME, tableName);
    }
}
