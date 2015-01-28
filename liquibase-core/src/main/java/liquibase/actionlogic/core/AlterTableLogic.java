package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class AlterTableLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RedefineTableAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RedefineTableAction.Attr.tableName, action)
                .checkForRequiredField(RedefineTableAction.Attr.newDefinition, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new RewriteResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeTableName(action.get(RedefineTableAction.Attr.catalogName, String.class), action.get(RedefineTableAction.Attr.schemaName, String.class), action.get(RedefineTableAction.Attr.tableName, String.class))
                + " "
                + action.get(RedefineTableAction.Attr.newDefinition, String.class).trim()));
    }
}
