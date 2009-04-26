package liquibase.database.statement;

import liquibase.database.Database;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class ReindexStatement implements SqlStatement {

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
