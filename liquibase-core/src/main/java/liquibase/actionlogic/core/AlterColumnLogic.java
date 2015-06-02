package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class AlterColumnLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AlterColumnAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(AlterColumnAction.Attr.columnName, action)
                .checkForRequiredContainer("Table name is required", AlterColumnAction.Attr.columnName, action)
                .checkForRequiredField(AlterColumnAction.Attr.newDefinition, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeObjectName(action.get(AlterColumnAction.Attr.columnName, ObjectName.class).getContainer(), Table.class)
                + " ALTER COLUMN "
                + database.escapeObjectName(action.get(AlterColumnAction.Attr.columnName, String.class), Column.class)
                + " "
                + action.get(AlterColumnAction.Attr.newDefinition, String.class).trim()));
    }
}
