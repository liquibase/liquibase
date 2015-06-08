package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class AlterTableLogic extends AbstractActionLogic<AlterTableAction> {

    @Override
    protected Class<AlterTableAction> getSupportedAction() {
        return AlterTableAction.class;
    }

    @Override
    public ValidationErrors validate(AlterTableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("newDefinition", action);
    }

    @Override
    public ActionResult execute(AlterTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeObjectName(action.tableName, Table.class)
                + " "
                + action.newDefinition.toString().trim()));
    }
}
