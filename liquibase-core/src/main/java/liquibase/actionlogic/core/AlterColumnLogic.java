package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;

public class AlterColumnLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AlterColumnAction.class;
    }

    @Override
    protected int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(AlterColumnAction.Attr.tableName, action)
                .checkForRequiredField(AlterColumnAction.Attr.columnName, action)
                .checkForRequiredField(AlterColumnAction.Attr.newDefinition, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new RewriteResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeTableName(action.get(AlterColumnAction.Attr.catalogName, String.class), action.get(AlterColumnAction.Attr.schemaName, String.class), action.get(AlterColumnAction.Attr.tableName, String.class))
                + " ALTER COLUMN "
                + database.escapeObjectName(action.get(AlterColumnAction.Attr.columnName, String.class), Column.class)
                + " "
                + action.get(AlterColumnAction.Attr.newDefinition, String.class).trim()));
    }
}
