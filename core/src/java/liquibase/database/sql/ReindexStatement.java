package liquibase.database.sql;

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

	public String getEndDelimiter(Database database) {
		return ";";
	}

	public String getSqlStatement(Database database)
			throws StatementNotSupportedOnDatabaseException {
		if (!supportsDatabase(database)) {
			throw new StatementNotSupportedOnDatabaseException(this, database);
		}
		return "REINDEX "+database.escapeTableName(getSchemaName(), getTableName());
	}

	public boolean supportsDatabase(Database database) {
		return (database instanceof SQLiteDatabase);
	}

}
