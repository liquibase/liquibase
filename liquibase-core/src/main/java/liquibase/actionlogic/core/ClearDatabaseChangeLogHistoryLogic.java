package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.ClearDatabaseChangeLogHistoryAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;

public class ClearDatabaseChangeLogHistoryLogic extends AbstractActionLogic<ClearDatabaseChangeLogHistoryAction> {

    @Override
    protected Class<ClearDatabaseChangeLogHistoryAction> getSupportedAction() {
        return ClearDatabaseChangeLogHistoryAction.class;
    }

    @Override
    public ActionResult execute(ClearDatabaseChangeLogHistoryAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        ObjectReference container = action.container;
        if (container == null) {
            container = new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
        }

        return new DelegateResult(new UpdateSqlAction("DELETE FROM " + database.escapeObjectName(new ObjectReference(container, database.getDatabaseChangeLogTableName()))));
    }
}
