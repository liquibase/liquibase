package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

public class DropDefaultValueLogic extends AbstractSqlBuilderLogic<DropDefaultValueAction> {

    @Override
    protected Class<DropDefaultValueAction> getSupportedAction() {
        return DropDefaultValueAction.class;
    }

    @Override
    public ValidationErrors validate(DropDefaultValueAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredField("columnName", action);
    }

    @Override
    public ActionResult execute(DropDefaultValueAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(DropDefaultValueAction action, Scope scope) {
        return new StringClauses().append("DEFAULT NULL");
    }
}
