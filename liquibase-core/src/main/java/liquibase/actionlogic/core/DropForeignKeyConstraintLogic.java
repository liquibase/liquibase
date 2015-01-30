package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropForeignKeyConstraintAction;
import liquibase.action.core.RedefineTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class DropForeignKeyConstraintLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropForeignKeyConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropForeignKeyConstraintAction.Attr.baseTableName, action)
                .checkForRequiredField(DropForeignKeyConstraintAction.Attr.constraintName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new RedefineTableAction(
                action.get(DropForeignKeyConstraintAction.Attr.baseTableCatalogName, String.class),
                action.get(DropForeignKeyConstraintAction.Attr.baseTableSchemaName, String.class),
                action.get(DropForeignKeyConstraintAction.Attr.baseTableName, String.class),
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("DROP CONSTRAINT")
                .append(database.escapeConstraintName(action.get(DropForeignKeyConstraintAction.Attr.constraintName, String.class)));
    }
}
