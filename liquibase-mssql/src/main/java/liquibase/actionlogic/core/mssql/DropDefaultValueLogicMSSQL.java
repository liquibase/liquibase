package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.DatabaseException;
import liquibase.structure.core.Table;

public class DropDefaultValueLogicMSSQL extends DropDefaultValueLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(DropDefaultValueAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        String escapedTableName = database.escapeObjectName(action.columnName.container, Table.class);

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
                    "DECLARE @sql [nvarchar](MAX)\r\n" +
                            "SELECT @sql = N'ALTER TABLE " + database.escapeStringForDatabase(escapedTableName) + " DROP CONSTRAINT ' + QUOTENAME([df].[name]) " +
                            "FROM [sys].[columns] AS [c] " +
                            "INNER JOIN [sys].[default_constraints] AS [df] " +
                            "ON [df].[object_id] = [c].[default_object_id] " +
                            "WHERE [c].[object_id] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) +  "') " +
                            "AND [c].[name] = N'" + database.escapeStringForDatabase(action.columnName.name) +  "'\r\n" +
                            "EXEC sp_executesql @sql";
        } else {
            // SQL Server 2000
            sql =
                    "DECLARE @sql [nvarchar](4000)\r\n" +
                            "SELECT @sql = N'ALTER TABLE " + database.escapeStringForDatabase(escapedTableName) + " DROP CONSTRAINT ' + QUOTENAME([df].[name]) " +
                            "FROM [dbo].[syscolumns] AS [c] " +
                            "INNER JOIN [dbo].[sysobjects] AS [df] " +
                            "ON [df].[id] = [c].[cdefault] " +
                            "WHERE [c].[id] = OBJECT_ID(N'" + database.escapeStringForDatabase(escapedTableName) +  "') " +
                            "AND [c].[name] = N'" + database.escapeStringForDatabase(action.columnName.name) +  "'\r\n" +
                            "EXEC sp_executesql @sql";
        }
        return new DelegateResult(new ExecuteSqlAction(sql));
    }
}
