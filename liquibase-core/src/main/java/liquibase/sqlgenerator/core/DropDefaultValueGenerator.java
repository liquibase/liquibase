package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.DropDefaultValueStatement;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class DropDefaultValueGenerator extends AbstractSqlGenerator<DropDefaultValueStatement> {

    @Override
    public boolean supports(DropDefaultValueStatement statement, Database database) {
        return !(database instanceof SQLiteDatabase);
    }

    @Override
    public ValidationErrors validate(DropDefaultValueStatement dropDefaultValueStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("tableName", dropDefaultValueStatement.getTableName());
        validationErrors.checkRequiredField("columnName", dropDefaultValueStatement.getColumnName());

        if (database instanceof InformixDatabase) {
            validationErrors.checkRequiredField("columnDataType", dropDefaultValueStatement.getColumnDataType());
        }


        return validationErrors;
    }

    @Override
    public Sql[] generateSql(DropDefaultValueStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String sql;
        String escapedTableName = database.escapeTableName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName());
        if (database instanceof MSSQLDatabase) {
            sql =
                    "DECLARE @sql [nvarchar](MAX)\r\n" +
                    "SELECT @sql = N'ALTER TABLE " + database.escapeStringForDatabase(escapedTableName) + " DROP CONSTRAINT ' + QUOTENAME([df].[name]) " +
                    "FROM [sys].[columns] AS [c] " +
                    "INNER JOIN [sys].[default_constraints] AS [df] " +
                    "ON [df].[object_id] = [c].[default_object_id] " +
                    "WHERE [c].[object_id] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) +  "') " +
                    "AND [c].[name] = N'" + database.escapeStringForDatabase(statement.getColumnName()) +  "'\r\n" +
                    "EXEC sp_executesql @sql";
        } else if (database instanceof MySQLDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else if (database instanceof OracleDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " MODIFY " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof SybaseDatabase) {
             sql = "ALTER TABLE " + escapedTableName + " REPLACE " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof SybaseASADatabase) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DEFAULT NULL";
        } else if (database instanceof DerbyDatabase) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " WITH DEFAULT NULL";
        } else if (database instanceof InformixDatabase) {
        	/*
        	 * TODO If dropped from a not null column the not null constraint will be dropped, too.
        	 * If the column is "NOT NULL" it has to be added behind the datatype.
        	 */
        	sql = "ALTER TABLE " + escapedTableName + " MODIFY (" + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " " + statement.getColumnDataType() + ")";
        } else if (database instanceof AbstractDb2Database) {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " DROP DEFAULT";
        } else {
            sql = "ALTER TABLE " + escapedTableName + " ALTER COLUMN  " + database.escapeColumnName(statement.getCatalogName(), statement.getSchemaName(), statement.getTableName(), statement.getColumnName()) + " SET DEFAULT NULL";
        }

        return new Sql[] {
                new UnparsedSql(sql, getAffectedColumn(statement))
        };
    }

    protected Column getAffectedColumn(DropDefaultValueStatement statement) {
        return new Column().setName(statement.getColumnName()).setRelation(new Table().setName(statement.getTableName()).setSchema(statement.getCatalogName(), statement.getSchemaName()));
    }
}
