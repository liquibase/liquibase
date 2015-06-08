package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.RenameTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class RenameTableLogic extends AbstractSqlBuilderLogic<RenameTableAction> {


    @Override
    protected Class<RenameTableAction> getSupportedAction() {
        return RenameTableAction.class;
    }

    @Override
    public ValidationErrors validate(RenameTableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("newTableName", action)
                .checkForRequiredField("oldTableName", action);
    }

    @Override
    public ActionResult execute(RenameTableAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.oldTableName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(RenameTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("RENAME TO")
                .append(database.escapeObjectName(action.newTableName, Table.class));
    }
}
