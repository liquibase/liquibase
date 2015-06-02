package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameViewAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameViewLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.View;

public class RenameViewLogicMSSQL extends RenameViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "exec sp_rename '"
                        + database.escapeObjectName(action.get(RenameViewAction.Attr.oldViewName, ObjectName.class), View.class)
                        + "', '"
                        + action.get(RenameViewAction.Attr.newViewName, String.class)
                        + "'"));
    }
}
