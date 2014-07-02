package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

/**
 * Creates an index on an existing table
 */
public class CreateIndexStatement extends AbstractStatement {

    private static final String TABLE_CATALOG_NAME = "tableCatalogName";
    private static final String TABLE_SCHEMA_NAME = "tableSchemaName";
    private static final String INDEX_NAME = "indexName";
    private static final String TABLE_NAME = "tableName";
    private static final String COLUMN_NAMES ="columnNames";
    private static final String TABLESPACE = "tablespace";
    private static final String UNIQUE = "unique";
	private static final String ASSOCIATED_WITH = "associatedWith";

    public CreateIndexStatement() {
    }

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, String... columns) {
        setIndexName(indexName);
        setTableCatalogName(tableCatalogName);
        setTableSchemaName(tableSchemaName);
        setTableName(tableName);
        setColumnNames(columns);
        setUnique(isUnique);
	    setAssociatedWith(associatedWith);
    }

    public String getTableCatalogName() {
        return getAttribute(TABLE_CATALOG_NAME, String.class);
    }

    public CreateIndexStatement setTableCatalogName(String tableCatalogName) {
        return (CreateIndexStatement) setAttribute(TABLE_CATALOG_NAME, tableCatalogName);
    }


    public String getTableSchemaName() {
        return getAttribute(TABLE_SCHEMA_NAME, String.class);
    }

    public CreateIndexStatement setTableSchemaName(String tableSchemaName) {
        return (CreateIndexStatement) setAttribute(TABLE_SCHEMA_NAME, tableSchemaName);
    }


    public String getIndexName() {
        return getAttribute(INDEX_NAME, String.class);
    }

    public CreateIndexStatement setIndexName(String indexName) {
        return (CreateIndexStatement) setAttribute(INDEX_NAME, indexName);
    }

    public String getTableName() {
        return getAttribute(TABLE_NAME, String.class);
    }

    public CreateIndexStatement setTableName(String tableName) {
        return (CreateIndexStatement) setAttribute(TABLE_NAME, tableName);
    }

    public String[] getColumnNames() {
        return getAttribute(COLUMN_NAMES, String[].class);
    }

    public CreateIndexStatement setColumnNames(String[] columns) {
        return (CreateIndexStatement) setAttribute(COLUMN_NAMES, columns);
    }

    public String getTablespace() {
        return getAttribute(TABLESPACE, String.class);
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        return (CreateIndexStatement) setAttribute(TABLESPACE, tablespace);
    }

    public Boolean isUnique() {
        return getAttribute(UNIQUE, Boolean.class);
    }

    public CreateIndexStatement setUnique(Boolean unique) {
        return (CreateIndexStatement) setAttribute(UNIQUE, unique);
    }


    public String getAssociatedWith() {
        return getAttribute(ASSOCIATED_WITH, String.class);
	}

	public CreateIndexStatement setAssociatedWith(String associatedWith) {
		return (CreateIndexStatement) setAttribute(ASSOCIATED_WITH, associatedWith);
	}

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Index().setName(getIndexName()).setTable((Table) new Table().setName(getTableName()).setSchema(getTableCatalogName(), getTableSchemaName()))
        };
    }
}