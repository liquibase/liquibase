package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropUniqueConstraintStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String constraintName;
    /**
     * Sybase ASA does drop unique constraint not by name, but using list of the columns in unique clause.
     */
    private String uniqueColumns;

    public DropUniqueConstraintStatement(String schemaName, String tableName, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
    }

    public DropUniqueConstraintStatement(String schemaName, String tableName, String constraintName, String uniqueColumns) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.uniqueColumns = uniqueColumns;
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
    	
    	if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP KEY " + database.escapeConstraintName(getConstraintName());
        } else if (database instanceof MaxDBDatabase) {
            return "DROP INDEX " + database.escapeConstraintName(getConstraintName()) + " ON " + database.escapeTableName(getSchemaName(), getTableName());
        } else if (database instanceof OracleDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(getConstraintName())+" DROP INDEX";
        } else if (database instanceof SybaseASADatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP UNIQUE (" + getUniqueColumns() + ")";
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + database.escapeConstraintName(getConstraintName());
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return !(database instanceof SQLiteDatabase);
    }

	public String getUniqueColumns() {
		return uniqueColumns;
	}

	public void setUniqueColumns(String uniqueColumns) {
		this.uniqueColumns = uniqueColumns;
	}

}
