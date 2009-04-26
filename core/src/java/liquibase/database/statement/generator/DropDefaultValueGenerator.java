package liquibase.database.statement.generator;

import liquibase.database.*;
import liquibase.database.statement.DropDefaultValueStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.statement.syntax.UnparsedSql;
import liquibase.exception.StatementNotSupportedOnDatabaseException;
import liquibase.exception.JDBCException;

public class DropDefaultValueGenerator implements SqlGenerator<DropDefaultValueStatement> {
    public int getSpecializationLevel() {
        return SPECIALIZATION_LEVEL_DEFAULT;
    }

    public boolean isValidGenerator(DropDefaultValueStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    public GeneratorValidationErrors validate(DropDefaultValueStatement dropDefaultValueStatement, Database database) {
        return new GeneratorValidationErrors();
    }

    public Sql[] generateSql(DropDefaultValueStatement statement, Database database) throws JDBCException {
        String sql;
         if (database instanceof MSSQLDatabase) {
        		if(database.getDatabaseProductVersion().startsWith("9")) { // SQL Server 2005
			      // SQL Server 2005 does not often work with the simpler query shown below
        			String query = "DECLARE @default sysname\n";
        			query += "SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('" + statement.getSchemaName() + "." + statement.getTableName() + "') AND name='" + statement.getColumnName() + "'\n";
        			query += "EXEC ('ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT ' + @default)";
        			//System.out.println("DROP QUERY : " + query);
        			sql = query;
        		} else {
        			sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " DROP CONSTRAINT select d.name from syscolumns c,sysobjects d, sysobjects t where c.id=t.id AND d.parent_obj=t.id AND d.type='D' AND t.type='U' AND c.name='"+statement.getColumnName()+"' AND t.name='"+statement.getTableName()+"'";
        		}
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof OracleDatabase || database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof DerbyDatabase) {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT NULL";
        } else if (database instanceof MaxDBDatabase) {
          	sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof InformixDatabase) {
        	/*
        	 * TODO If dropped from a not null column the not null constraint will be dropped, too.
        	 * If the column is "NOT NULL" it has to be added behind the datatype.
        	 */
        	if (statement.getColumnDataType() == null) {
                throw new StatementNotSupportedOnDatabaseException("Database requires columnDataType parameter", statement, database);
        	}
        	sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " MODIFY (" + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + ")";
        } else {
            sql = "ALTER TABLE " + database.escapeTableName(statement.getSchemaName(), statement.getTableName()) + " ALTER COLUMN  " + database.escapeColumnName(statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT NULL";
         }

        return new Sql[] {
                new UnparsedSql(sql)
        };
    }
}
