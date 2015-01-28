package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RedefineColumnAction;
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
        return RedefineColumnAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RedefineColumnAction.Attr.tableName, action)
                .checkForRequiredField(RedefineColumnAction.Attr.columnName, action)
                .checkForRequiredField(RedefineColumnAction.Attr.newDefinition, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new RewriteResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeTableName(action.get(RedefineColumnAction.Attr.catalogName, String.class), action.get(RedefineColumnAction.Attr.schemaName, String.class), action.get(RedefineColumnAction.Attr.tableName, String.class))
                + " ALTER COLUMN "
                + database.escapeObjectName(action.get(RedefineColumnAction.Attr.columnName, String.class), Column.class)
                + " "
                + action.get(RedefineColumnAction.Attr.newDefinition, String.class).trim()));
    }
}
