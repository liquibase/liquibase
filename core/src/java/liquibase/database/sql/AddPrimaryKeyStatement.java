package liquibase.database.sql;

import liquibase.database.DB2Database;
import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.SQLiteDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.util.StringUtils;

public class AddPrimaryKeyStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyStatement(String schemaName, String tableName, String columnNames, String constraintName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.constraintName = constraintName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTablespace() {
        return tablespace;
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }
    	
        String sql;
        if (getConstraintName() == null  || database instanceof MySQLDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD PRIMARY KEY (" + database.escapeColumnNameList(getColumnNames()) + ")";
        } else if (database instanceof InformixDatabase) {
        	sql = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD CONSTRAINT PRIMARY KEY (" + database.escapeColumnNameList(getColumnNames()) + ") CONSTRAINT " + database.escapeConstraintName(getConstraintName());
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ADD CONSTRAINT " + database.escapeConstraintName(getConstraintName()) + " PRIMARY KEY (" + database.escapeColumnNameList(getColumnNames()) + ")";
        }

        if (StringUtils.trimToNull(getTablespace()) != null && database.supportsTablespaces()) {
            if (database instanceof MSSQLDatabase ) {
                sql += " ON "+getTablespace();
            } else if (database instanceof DB2Database || database instanceof SybaseASADatabase) {
                ; //not supported
            } else {
                sql += " USING INDEX TABLESPACE "+getTablespace();
            }
        }

        return sql;
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return (!(database instanceof SQLiteDatabase));
    }
}
