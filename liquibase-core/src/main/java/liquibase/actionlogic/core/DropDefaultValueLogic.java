package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class DropDefaultValueLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropDefaultValueAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredContainer("Table name is required", DropDefaultValueAction.Attr.columnName, action)
                .checkForRequiredField(DropDefaultValueAction.Attr.columnName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.get(DropDefaultValueAction.Attr.columnName, ObjectName.class),
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return new StringClauses().append("DEFAULT NULL");
    }
}
