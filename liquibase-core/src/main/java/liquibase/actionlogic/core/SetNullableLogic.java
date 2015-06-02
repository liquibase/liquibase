package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.SetNullableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

public class SetNullableLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return SetNullableAction.class;
    }

    @Override
    public ValidationErrors validate(final Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(SetNullableAction.Attr.columnName, action)
                .checkForRequiredContainer("Table name is required", SetNullableAction.Attr.columnName, action)
                .checkForRequiredContainer("Table name is required", SetNullableAction.Attr.columnName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.get(SetNullableAction.Attr.columnName, ObjectName.class),
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        if (action.get(SetNullableAction.Attr.nullable, false)) {
            return new StringClauses().append("NULL");
        } else {
            return new StringClauses().append("NOT NULL");
        }
    }
}
