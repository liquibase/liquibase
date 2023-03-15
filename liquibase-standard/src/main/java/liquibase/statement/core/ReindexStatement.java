package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ReindexStatement extends AbstractSqlStatement {

    private String catalogName;
	private String schemaName;
    private String tableName;
    
	public ReindexStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
		this.schemaName = schemaName;
        this.tableName = tableName;
	}

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }
}
