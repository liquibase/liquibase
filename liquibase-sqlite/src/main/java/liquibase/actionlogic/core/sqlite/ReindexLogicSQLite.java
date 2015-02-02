package liquibase.actionlogic.core.sqlite;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ReindexAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.database.core.sqlite.SQLiteDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class ReindexLogicSQLite extends AbstractActionLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SQLiteDatabase.class;
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return ReindexAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(ReindexAction.Attr.tableName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new DelegateResult(new ExecuteSqlAction(
                "REINDEX "
                        + database.escapeTableName(action.get(ReindexAction.Attr.catalogName, String.class),
                        action.get(ReindexAction.Attr.schemaName, String.class),
                        action.get(ReindexAction.Attr.tableName, String.class))));
    }
}
