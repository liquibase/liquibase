package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.SetColumnRemarksAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class SetColumnRemarksLogic extends AbstractActionLogic<SetColumnRemarksAction> {

    @Override
    protected Class<SetColumnRemarksAction> getSupportedAction() {
        return SetColumnRemarksAction.class;
    }

    @Override
    public ValidationErrors validate(SetColumnRemarksAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredField("columnName", action);
    }

    @Override
    public ActionResult execute(SetColumnRemarksAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new ExecuteSqlAction(
                "COMMENT ON COLUMN "
                        + database.escapeObjectName(action.columnName.container)
                        + "."
                        + database.escapeObjectName(action.columnName)
                        + " IS '"
                        + database.escapeStringForDatabase(action.remarks)
                        + "'"
        ));
    }
}
