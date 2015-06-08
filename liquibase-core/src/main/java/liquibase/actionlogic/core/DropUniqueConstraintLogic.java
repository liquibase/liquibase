package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class DropUniqueConstraintLogic extends AbstractSqlBuilderLogic<DropUniqueConstraintActon> {

    @Override
    protected Class<DropUniqueConstraintActon> getSupportedAction() {
        return DropUniqueConstraintActon.class;
    }

    @Override
    public ValidationErrors validate(DropUniqueConstraintActon action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("tableName", action)
                .checkForRequiredField("constraintName", action);
    }

    @Override
    public ActionResult execute(DropUniqueConstraintActon action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.tableName,
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(DropUniqueConstraintActon action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP CONSTRAINT")
                .append(database.escapeConstraintName(action.constraintName));

    }
}
