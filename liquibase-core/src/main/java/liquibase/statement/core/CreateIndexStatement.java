package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateIndexStatement extends AbstractSqlStatement {

    private String tableSchemaName;
    private String indexName;
    private String tableName;
    private String[] columns;
    private String tablespace;
    private Boolean unique;
	// Contain associations of index
	// for example: foreignKey, primaryKey or uniqueConstraint
	private String associatedWith;

    public CreateIndexStatement(String indexName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, String... columns) {
        this.indexName = indexName;
        this.tableSchemaName = tableSchemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.unique = isUnique;
	    this.associatedWith = associatedWith;
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
}