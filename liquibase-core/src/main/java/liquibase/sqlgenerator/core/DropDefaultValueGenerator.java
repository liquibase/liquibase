package liquibase.sqlgenerator.core;

import liquibase.action.Action;
import liquibase.action.UnparsedSql;
import liquibase.actiongenerator.ActionGeneratorChain;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropDefaultValueStatement;

public class DropDefaultValueGenerator extends AbstractSqlGenerator<DropDefaultValueStatement> {

    @Override
    public boolean supports(DropDefaultValueStatement statement, ExecutionOptions options) {
        return !(options.getRuntimeEnvironment().getTargetDatabase() instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(DropDefaultValueStatement dropDefaultValueStatement, ExecutionOptions options, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropDefaultValueStatement.getTableName());
        validationErrors.checkRequiredField("columnName", dropDefaultValueStatement.getColumnName());

        if (options.getRuntimeEnvironment().getTargetDatabase() instanceof InformixDatabase) {
            validationErrors.checkRequiredField("columnDataType", dropDefaultValueStatement.getColumnDataType());
        }


        return validationErrors;
    }

    @Override
    public Action[] generateActions(DropDefaultValueStatement statement, ExecutionOptions options, ActionGeneratorChain chain) {
        Database database = options.getRuntimeEnvironment().getTargetDatabase();
        String sql;
        String escapedTableName = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
        if (database instanceof MSSQLDatabase) {
             String productVersion = null;
             try {
                 productVersion = database.getDatabaseProductVersion();
             } catch (DatabaseException e) {
                 throw new UnexpectedLiquibaseException(e);
             }
             if(productVersion == null || productVersion.startsWith("9") || productVersion.startsWith("10") || productVersion.startsWith("11") || productVersion.startsWith("12")) { // SQL Server 2005/2008/2012/2014
                // SQL Server 2005 does not often work with the simpler query shown below
                String query = "DECLARE @default sysname\n";
                query += "SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('" + escapedTableName + "') AND name='" + statement.getColumnName() + "'\n";
                query += "EXEC ('ALTER TABLE " + escapedTableName + " DROP CONSTRAINT ' + @default)";
                // System.out.println("DROP QUERY : " + query);
                sql = query;
             } else {
        		// FIXME this syntax does not supported by MSSQL 2000
        		sql = "ALTER TABLE " + escapedTableName + " DROP CONSTRAINT select d.name from syscolumns c,sysobjects d, sysobjects t where c.id=t.id AND d.parent_obj=t.id AND d.type='D' AND t.type='U' AND c.name='"+statement.getColumnName()+"' AND t.name='"+statement.getTableName()+"'";
             }
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof SybaseASADatabase) {
             sql = "ALTER TABLE " + escapedTableName + " REPLACE " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof DerbyDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT NULL";
        } else if (database instanceof InformixDatabase) {
        	/*
        	 * TODO If dropped from a not null column the not null constraint will be dropped, too.
        	 * If the column is "NOT NULL" it has to be added behind the datatype.
        	 */
        	sql = "ALTER TABLE " + escapedTableName + " MODIFY (" + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + ")";
        } else if (database instanceof DB2Database) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT NULL";
         }

        return new Action[] {
                new UnparsedSql(sql)
        };
    }
}
