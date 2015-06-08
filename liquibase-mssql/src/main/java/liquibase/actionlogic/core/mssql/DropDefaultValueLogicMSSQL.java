package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
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

        return new DelegateResult(new ExecuteSqlAction("DECLARE @default sysname\n"
                + "SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('" + escapedTableName + "') AND name='" + action.columnName + "'\n"
                + "EXEC ('ALTER TABLE " + escapedTableName + " DROP CONSTRAINT ' + @default)"));
    }
}
