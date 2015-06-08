package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.UpdateSqlAction;
import liquibase.action.core.ClearDatabaseChangeLogHistoryAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class ClearDatabaseChangeLogHistoryLogic extends AbstractActionLogic<ClearDatabaseChangeLogHistoryAction> {

    @Override
    protected Class<ClearDatabaseChangeLogHistoryAction> getSupportedAction() {
        return ClearDatabaseChangeLogHistoryAction.class;
    }

    @Override
    public ActionResult execute(ClearDatabaseChangeLogHistoryAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        ObjectName container = action.container;
        if (container == null) {
            container = new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName());
        }

        return new DelegateResult(new UpdateSqlAction("DELETE FROM " + database.escapeObjectName(new ObjectName(container, database.getDatabaseChangeLogTableName()), Table.class)));
    }
}
