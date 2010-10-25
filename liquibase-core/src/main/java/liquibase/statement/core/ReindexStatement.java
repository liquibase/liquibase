package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ReindexStatement extends AbstractSqlStatement {

	private String schemaName;
    private String tableName;
    
	public ReindexStatement(String schemaName, String tableName) {
		this.schemaName = schemaName;
        this.tableName = tableName;
	}
	
	public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }
}
