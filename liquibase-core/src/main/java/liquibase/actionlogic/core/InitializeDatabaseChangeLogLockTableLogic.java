package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DeleteDataAction;
import liquibase.action.core.InitializeDatabaseChangeLogLockTableAction;
import liquibase.action.core.InsertDataAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

public class InitializeDatabaseChangeLogLockTableLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return InitializeDatabaseChangeLogLockTableAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        ObjectName tableName = new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());
        return new DelegateResult(
                new DeleteDataAction(tableName),
                new InsertDataAction(tableName)
                        .addColumnValue("ID", 1)
                        .addColumnValue("LOCKED", Boolean.FALSE)

        );
    }
}
