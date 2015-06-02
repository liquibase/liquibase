package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameTableAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class RenameTableLogicMSSQL extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "exec sp_rename '"
                        + database.escapeObjectName(action.get(RenameTableAction.Attr.oldTableName, ObjectName.class), Table.class)
                        + "', '"
                        + action.get(RenameTableAction.Attr.newTableName, String.class)
                        + "'"));
    }
}
