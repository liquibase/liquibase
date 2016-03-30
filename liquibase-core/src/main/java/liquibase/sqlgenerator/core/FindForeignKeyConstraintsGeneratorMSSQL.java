package liquibase.sqlgenerator.core;

import static liquibase.statement.core.FindForeignKeyConstraintsStatement.*;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.core.FindForeignKeyConstraintsStatement;
import liquibase.structure.core.Column;

public class FindForeignKeyConstraintsGeneratorMSSQL extends AbstractSqlGenerator<FindForeignKeyConstraintsStatement> {
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(FindForeignKeyConstraintsStatement statement, Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    public ValidationErrors validate(FindForeignKeyConstraintsStatement findForeignKeyConstraintsStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkRequiredField("baseTableName", findForeignKeyConstraintsStatement.getBaseTableName());
        return validationErrors;
    }

    @Override
    public Sql[] generateSql(FindForeignKeyConstraintsStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        String escapedTableName = database.escapeTableName(statement.getBaseTableCatalogName(), statement.getBaseTableSchemaName(), statement.getBaseTableName());
        boolean sql2005OrLater = true;
        try {
            sql2005OrLater = database.getDatabaseMajorVersion() >= 9;
        } catch (DatabaseException e) {
            // Assume SQL Server 2005 or later
        }
        String sql;
        if (sql2005OrLater) {
            // SQL Server 2005 or later
            sql =
                "SELECT " +
                    "OBJECT_NAME([fk].[parent_object_id]) AS " + database.escapeObjectName(RESULT_COLUMN_BASE_TABLE_NAME, Column.class) + ", " +
                    "COL_NAME([fkc].[parent_object_id], [fkc].[parent_column_id]) AS " + database.escapeObjectName(RESULT_COLUMN_BASE_TABLE_COLUMN_NAME, Column.class) + ", " +
                    "OBJECT_NAME([fk].[referenced_object_id]) AS " + database.escapeObjectName(RESULT_COLUMN_FOREIGN_TABLE_NAME, Column.class) + ", " +
                    "COL_NAME([fkc].[referenced_object_id], [fkc].[referenced_column_id]) AS " + database.escapeObjectName(RESULT_COLUMN_FOREIGN_COLUMN_NAME, Column.class) + ", " +
                    "[fk].[name] AS " + database.escapeObjectName(RESULT_COLUMN_CONSTRAINT_NAME, Column.class) + " " +
                "FROM [sys].[foreign_keys] AS [fk] " +
                "INNER JOIN [sys].[foreign_key_columns] AS [fkc] " +
                "ON [fk].[object_id] = [fkc].[constraint_object_id] " +
                "WHERE [fk].[parent_object_id] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) + "') " +
                "ORDER BY " +
                    "[fk].[name], " +
                    "[fkc].[constraint_column_id]";
        } else {
            // SQL Server 2000
            sql =
                "SELECT " +
                    "OBJECT_NAME([fkc].[fkeyid]) AS " + database.escapeObjectName(RESULT_COLUMN_BASE_TABLE_NAME, Column.class) + ", " +
                    "COL_NAME([fkc].[fkeyid], [fkc].[fkey]) AS " + database.escapeObjectName(RESULT_COLUMN_BASE_TABLE_COLUMN_NAME, Column.class) + ", " +
                    "OBJECT_NAME([fkc].[rkeyid]) AS " + database.escapeObjectName(RESULT_COLUMN_FOREIGN_TABLE_NAME, Column.class) + ", " +
                    "COL_NAME([fkc].[rkeyid], [fkc].[rkey]) AS " + database.escapeObjectName(RESULT_COLUMN_FOREIGN_COLUMN_NAME, Column.class) + ", " +
                    "[fk].[name] AS " + database.escapeObjectName(RESULT_COLUMN_CONSTRAINT_NAME, Column.class) + " " +
                "FROM [dbo].[sysobjects] AS [fk] " +
                "INNER JOIN [dbo].[sysforeignkeys] AS [fkc] " +
                "ON [fkc].[constid] = [fk].[id] " +
                "WHERE [fk].[xtype] = 'F' " +
                "AND [fk].[parent_obj] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) + "') " +
                "ORDER BY " +
                    "[fk].[name], " +
                    "[fkc].[keyno]";
        }

        return new Sql[] { new UnparsedSql(sql) };
    }
}