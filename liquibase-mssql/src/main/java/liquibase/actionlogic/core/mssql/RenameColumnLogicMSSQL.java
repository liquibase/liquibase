package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Column;

public class RenameColumnLogicMSSQL extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        // do no escape the new column name. Otherwise it produce "exec sp_rename '[dbo].[person].[usernae]', '[username]'"
        return new DelegateResult(new ExecuteSqlAction(
                "exec sp_rename '"
                        + database.escapeTableName(action.get(RenameColumnAction.Attr.catalogName, String.class),
                        action.get(RenameColumnAction.Attr.schemaName, String.class),
                        action.get(RenameColumnAction.Attr.tableName, String.class))
                        + "."
                        + database.escapeObjectName(action.get(RenameColumnAction.Attr.oldColumnName, String.class), Column.class)
                        + "', '"
                        + action.get(RenameColumnAction.Attr.newColumnName, String.class)
                        + "'"));
    }
}
