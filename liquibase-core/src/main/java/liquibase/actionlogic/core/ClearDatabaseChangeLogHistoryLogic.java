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

public class ClearDatabaseChangeLogHistoryLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return ClearDatabaseChangeLogHistoryAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String schemaName = action.get(ClearDatabaseChangeLogHistoryAction.Attr.schemaName, String.class);
        if (schemaName == null) {
            schemaName = database.getLiquibaseSchemaName();
        }

        return new DelegateResult(new UpdateSqlAction("DELETE FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), schemaName, database.getDatabaseChangeLogTableName())));
    }
}
