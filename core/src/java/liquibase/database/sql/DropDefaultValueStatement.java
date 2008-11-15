package liquibase.database.sql;

import liquibase.database.*;
import liquibase.exception.StatementNotSupportedOnDatabaseException;

public class DropDefaultValueStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;

    public DropDefaultValueStatement(String schemaName, String tableName, String columnName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getSqlStatement(Database database) throws StatementNotSupportedOnDatabaseException {
    	if (!supportsDatabase(database)) {
            throw new StatementNotSupportedOnDatabaseException(this, database);
        }
    	
        if (database instanceof MSSQLDatabase) {
//smarter drop        return new SqlStatement[]{new RawSqlStatement("ALTER TABLE " + SqlUtil.escapeTableName(getTableName(), database) + " DROP CONSTRAINT select d.name from syscolumns c,sysobjects d, sysobjects t where c.id=t.id AND d.parent_obj=t.id AND d.type='D' AND t.type='U' AND c.name='"+getColumnName()+"' AND t.name='"+getTableName()+"'"),};
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " DROP CONSTRAINT " + ((MSSQLDatabase) database).generateDefaultConstraintName(getTableName(), getColumnName());
        } else if (database instanceof MySQLDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof OracleDatabase || database instanceof SybaseASADatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " MODIFY " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof DerbyDatabase) {
            return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " WITH DEFAULT NULL";
        } else if (database instanceof MaxDBDatabase) {
          	return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " DROP DEFAULT";
        }

        return "ALTER TABLE " + database.escapeTableName(getSchemaName(), getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(getSchemaName(), getTableName(), getColumnName()) + " SET DEFAULT NULL";
    }

    public String getEndDelimiter(Database database) {
        return ";";
    }

    public boolean supportsDatabase(Database database) {
    	return !(database instanceof SQLiteDatabase);
    }
}
