package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.SelectDataAction;
import liquibase.action.core.SelectFromDatabaseChangeLogAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;

public class SelectFromDatabaseChangeLogLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SelectFromDatabaseChangeLogAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        final Database database = scope.get(Scope.Attr.database, Database.class);

        return new DelegateResult(
                (SelectDataAction) new SelectDataAction(
                        database.getLiquibaseCatalogName(),
                        database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogTableName(),
                        action.get(SelectFromDatabaseChangeLogAction.Attr.selectColumnDefinitions, ColumnDefinition[].class))
                        .set(SelectDataAction.Attr.where, action.get(SelectFromDatabaseChangeLogAction.Attr.where, String.class))
                        .set(SelectDataAction.Attr.orderByColumnNames, action.get(SelectFromDatabaseChangeLogAction.Attr.orderByColumnNames, Object.class))
                        .set(SelectDataAction.Attr.limit, action.get(SelectFromDatabaseChangeLogAction.Attr.limit, Integer.class))
        );
    }
}