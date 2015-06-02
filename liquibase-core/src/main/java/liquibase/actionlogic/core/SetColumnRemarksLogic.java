package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.SetColumnRemarksAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

public class SetColumnRemarksLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SetColumnRemarksAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredContainer("Table name is required", SetColumnRemarksAction.Attr.columnName, action)
                .checkForRequiredField(SetColumnRemarksAction.Attr.columnName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction(
                "COMMENT ON COLUMN "
                        + database.escapeObjectName(action.get(SetColumnRemarksAction.Attr.columnName, ObjectName.class).getContainer(), Table.class)
                        + "."
                        + database.escapeObjectName(action.get(SetColumnRemarksAction.Attr.columnName, String.class), Column.class)
                        + " IS '"
                        + database.escapeStringForDatabase(action.get(SetColumnRemarksAction.Attr.remarks, String.class))
                        + "'"
        ));
    }
}
