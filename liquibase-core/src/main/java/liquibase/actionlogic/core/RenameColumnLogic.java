package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.RenameColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;

public class RenameColumnLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        oldColumnName,
        newColumnName
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RenameColumnAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RenameColumnAction.Attr.tableName, action)
                .checkForRequiredField(RenameColumnAction.Attr.oldColumnName, action)
                .checkForRequiredField(RenameColumnAction.Attr.newColumnName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new RedefineTableAction(
                action.get(RenameColumnAction.Attr.catalogName, String.class),
                action.get(RenameColumnAction.Attr.schemaName, String.class),
                action.get(RenameColumnAction.Attr.tableName, String.class),
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new StringClauses()
                .append("RENAME COLUMN")
                .append(Clauses.oldColumnName, database.escapeObjectName(action.get(RenameColumnAction.Attr.oldColumnName, String.class), Column.class))
                .append("TO")
                .append(Clauses.newColumnName, database.escapeObjectName(action.get(RenameColumnAction.Attr.oldColumnName, String.class), Column.class));
    }
}
