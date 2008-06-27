package liquibase.database.sql;

import liquibase.database.Database;
import liquibase.database.FirebirdDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropPrimaryKeyStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String constraintName;

    public DropPrimaryKeyStatement(String schemaName, String tableName, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }
    	
    	if (getConstraintName() == null) {
            if (database instanceof MSSQLDatabase
                    || database instanceof PostgresDatabase
                    || database instanceof FirebirdDatabase) {
                throw new StatementNotSupportedOnDatabaseException("Database requires a constraint name to drop the primary key", this, database);
            }
        }

        if (database instanceof MSSQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + getConstraintName();
        } else if (database instanceof PostgresDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + getConstraintName();
        } else if (database instanceof FirebirdDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT "+getConstraintName();
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP PRIMARY KEY";
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return (!(database instanceof SQLiteDatabase));
    }
}
