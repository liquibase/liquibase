package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.SelectDataAction;
import liquibase.action.core.SelectFromDatabaseChangeLogAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

public class SelectFromDatabaseChangeLogLogic extends AbstractActionLogic<SelectFromDatabaseChangeLogAction> {

    @Override
    protected Class<SelectFromDatabaseChangeLogAction> getSupportedAction() {
        return SelectFromDatabaseChangeLogAction.class;
    }

    @Override
    public ActionResult execute(SelectFromDatabaseChangeLogAction action, Scope scope) throws ActionPerformException {
        final Database database = scope.getDatabase();

        SelectDataAction selectDataAction = new SelectDataAction(
                new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()),
                action.selectColumns
        );
        selectDataAction.where = action.where;
        selectDataAction.orderByColumnNames = action.orderByColumnNames;
        selectDataAction.limit = action.limit;

        return new DelegateResult(selectDataAction);
    }
}