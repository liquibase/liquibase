package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.QuerySqlAction;
import liquibase.action.core.TableRowCountAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Table;

public class TableRowCountLogic extends AbstractActionLogic<TableRowCountAction> {

    @Override
    protected Class<TableRowCountAction> getSupportedAction() {
        return TableRowCountAction.class;
    }

    @Override
    public ValidationErrors validate(TableRowCountAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action);
    }

    @Override
    public ActionResult execute(TableRowCountAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        return new DelegateResult(new QuerySqlAction("SELECT COUNT(*) FROM "
                + database.escapeObjectName(action.tableName, Table.class)));
    }
}
