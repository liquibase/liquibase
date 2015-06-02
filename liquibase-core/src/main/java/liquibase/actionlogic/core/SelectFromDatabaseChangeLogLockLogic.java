package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.SelectDataAction;
import liquibase.action.core.SelectFromDatabaseChangeLogLockAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class SelectFromDatabaseChangeLogLockLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SelectFromDatabaseChangeLogLockAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(SelectFromDatabaseChangeLogLockAction.Attr.selectColumnDefinitions, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        final Database database = scope.get(Scope.Attr.database, Database.class);

        return new DelegateResult(
                (SelectDataAction) new SelectDataAction(
                        new ObjectName(database.getLiquibaseCatalogName(),
                                database.getLiquibaseSchemaName(),
                                database.getDatabaseChangeLogTableName()),
                        action.get(SelectFromDatabaseChangeLogLockAction.Attr.selectColumnDefinitions, ColumnDefinition[].class))
        );
    }

}
