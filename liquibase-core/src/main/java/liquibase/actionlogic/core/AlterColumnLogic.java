package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddColumnsAction;
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

public class AlterColumnLogic extends AbstractActionLogic<AlterColumnAction> {

    @Override
    protected Class<AlterColumnAction> getSupportedAction() {
        return AlterColumnAction.class;
    }

    @Override
    public ValidationErrors validate(AlterColumnAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("columnName", action)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredField("newDefinition", action);
    }

    @Override
    public ActionResult execute(AlterColumnAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeObjectName(action.columnName.container, Table.class)
                + " ALTER COLUMN "
                + database.escapeObjectName(action.columnName.name, Column.class)
                + " "
                + action.newDefinition.toString().trim()));
    }
}
