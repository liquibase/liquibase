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

public class RenameTableLogic extends AbstractSqlBuilderLogic {


    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RenameTableAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RenameTableAction.Attr.newTableName, action)
                .checkForRequiredField(RenameTableAction.Attr.oldTableName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.get(RenameTableAction.Attr.oldTableName, ObjectName.class),
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("RENAME TO")
                .append(database.escapeObjectName(action.get(RenameTableAction.Attr.newTableName, String.class), Table.class));
    }
}
