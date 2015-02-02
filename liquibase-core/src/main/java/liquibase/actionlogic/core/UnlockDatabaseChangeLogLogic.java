package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.UnlockDatabaseChangeLogAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Column;

public class UnlockDatabaseChangeLogLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return UnlockDatabaseChangeLogAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new DelegateResult((UpdateDataAction) new UpdateDataAction(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
        .addNewColumnValue("LOCKED", false)
        .addNewColumnValue("LOCKGRANTED", null)
        .addNewColumnValue("LOCKEDBY", null)
        .set(UpdateDataAction.Attr.whereClause, database.escapeObjectName("ID", Column.class) + " = 1"));
    }
}
