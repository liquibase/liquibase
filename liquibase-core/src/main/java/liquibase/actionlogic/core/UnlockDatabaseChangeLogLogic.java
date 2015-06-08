package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.action.core.UnlockDatabaseChangeLogAction;
import liquibase.action.core.UpdateDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;

public class UnlockDatabaseChangeLogLogic extends AbstractActionLogic<UnlockDatabaseChangeLogAction> {

    @Override
    protected Class<UnlockDatabaseChangeLogAction> getSupportedAction() {
        return UnlockDatabaseChangeLogAction.class;
    }

    @Override
    public ActionResult execute(UnlockDatabaseChangeLogAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        UpdateDataAction updateDataAction = new UpdateDataAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()))
                .addNewColumnValue("LOCKED", false)
                .addNewColumnValue("LOCKGRANTED", null)
                .addNewColumnValue("LOCKEDBY", null);
        updateDataAction.whereClause = new StringClauses(database.escapeObjectName("ID", Column.class) + " = 1");

        return new DelegateResult(updateDataAction);
    }
}
