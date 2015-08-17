package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropForeignKeyAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Index;
import liquibase.util.StringClauses;

public class DropForeignKeyLogic extends AbstractSqlBuilderLogic<DropForeignKeyAction> {

    @Override
    protected Class<DropForeignKeyAction> getSupportedAction() {
        return DropForeignKeyAction.class;
    }

    @Override
    public ValidationErrors validate(DropForeignKeyAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("baseTableName", action)
                .checkForRequiredField("constraintName", action);
    }

    @Override
    public ActionResult execute(DropForeignKeyAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.baseTableName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP CONSTRAINT")
                .append(database.escapeObjectName(action.constraintName, Index.class));
    }
}
