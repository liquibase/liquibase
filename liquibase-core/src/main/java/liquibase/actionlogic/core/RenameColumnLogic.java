package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringClauses;

public class RenameColumnLogic extends AbstractSqlBuilderLogic<RenameColumnAction> {

    public static enum Clauses {
        oldColumnName,
        newColumnName
    }

    @Override
    protected Class<RenameColumnAction> getSupportedAction() {
        return RenameColumnAction.class;
    }

    @Override
    public ValidationErrors validate(RenameColumnAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("oldColumnName", action)
                .checkForRequiredField("newColumnName", action);
    }

    @Override
    public ActionResult execute(RenameColumnAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.tableName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses()
                .append("RENAME COLUMN")
                .append(Clauses.oldColumnName, database.escapeObjectName(action.oldColumnName, Column.class))
                .append("TO")
                .append(Clauses.newColumnName, database.escapeObjectName(action.newColumnName, Column.class));
    }
}
