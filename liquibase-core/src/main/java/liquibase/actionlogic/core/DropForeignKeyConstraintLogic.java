package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropForeignKeyConstraintAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class DropForeignKeyConstraintLogic extends AbstractSqlBuilderLogic<DropForeignKeyConstraintAction> {

    @Override
    protected Class<DropForeignKeyConstraintAction> getSupportedAction() {
        return DropForeignKeyConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(DropForeignKeyConstraintAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("baseTableName", action)
                .checkForRequiredField("constraintName", action);
    }

    @Override
    public ActionResult execute(DropForeignKeyConstraintAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.baseTableName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyConstraintAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP CONSTRAINT")
                .append(database.escapeConstraintName(action.constraintName));
    }
}
