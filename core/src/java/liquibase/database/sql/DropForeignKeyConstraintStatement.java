package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.MaxDBDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropForeignKeyConstraintStatement implements SqlStatement {

    private String baseTableSchemaName;
    private String baseTableName;
    private String constraintName;

    public DropForeignKeyConstraintStatement(String baseTableSchemaName, String baseTableName, String constraintName) {
        this.baseTableSchemaName = baseTableSchemaName;
        this.baseTableName = baseTableName;
        this.constraintName = constraintName;
    }

    public String getBaseTableSchemaName() {
        return baseTableSchemaName;
    }

    public String getBaseTableName() {
        return baseTableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
    		throw new StatementNotSupportedOnDatabaseException("SQLite " +
    				"database does not support a drop foreign key statement", 
    				this, database);
    	}
    	
    	if (getBaseTableSchemaName() != null && !database.supportsSchemas()) {
            throw new StatementNotSupportedOnDatabaseException("Database does not support schemas", this, database);
        }
        
        if (database instanceof MySQLDatabase || database instanceof MaxDBDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " DROP FOREIGN KEY " + database.escapeConstraintName(getConstraintName());
        } else {
            return "ALTER TABLE " + database.escapeTableName(getBaseTableSchemaName(), getBaseTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(getConstraintName());
        }
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return (!(database instanceof SQLiteDatabase));
    }
}
