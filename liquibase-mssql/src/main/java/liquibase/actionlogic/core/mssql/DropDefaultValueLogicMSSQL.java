package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;

public class DropDefaultValueLogicMSSQL extends DropDefaultValueLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String escapedTableName = database.escapeTableName(
                action.get(DropDefaultValueAction.Attr.catalogName, String.class),
                action.get(DropDefaultValueAction.Attr.schemaName, String.class),
                action.get(DropDefaultValueAction.Attr.tableName, String.class));

        return new RewriteResult(new ExecuteSqlAction("DECLARE @default sysname\n"
                + "SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('" + escapedTableName + "') AND name='" + action.get(DropDefaultValueAction.Attr.columnName, String.class) + "'\n"
                + "EXEC ('ALTER TABLE " + escapedTableName + " DROP CONSTRAINT ' + @default)"));
    }
}
