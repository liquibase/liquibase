package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class CreateIndexStatement extends AbstractSqlStatement {

    private String tableCatalogName;
    private String tableSchemaName;
    private String indexName;
    private String tableName;
    private String[] columns;
    private String tablespace;
    private Boolean unique;
	// Contain associations of index
	// for example: foreignKey, primaryKey or uniqueConstraint
	private String associatedWith;

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, String... columns) {
        this.indexName = indexName;
        this.tableCatalogName = tableCatalogName;
        this.tableSchemaName = tableSchemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.unique = isUnique;
	    this.associatedWith = associatedWith;
    }

    public String getTableCatalogName() {
        return tableCatalogName;
    }

    public String getTableSchemaName() {
        return tableSchemaName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String[] getColumns() {
        return columns;
    }

    public String getTablespace() {
        return tablespace;
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;

        return this;
    }

    public Boolean isUnique() {
        return unique;
    }

	public String getAssociatedWith() {
		return associatedWith;
	}

	public void setAssociatedWith(String associatedWith) {
		this.associatedWith = associatedWith;
	}

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Index().setName(getIndexName()).setTable((Table) new Table().setName(getTableName()).setSchema(getTableCatalogName(), getTableSchemaName()))
        };
    }
}