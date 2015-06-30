package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class SetNullableLogic extends AbstractSqlBuilderLogic<SetNullableAction> {

    @Override
    protected Class<SetNullableAction> getSupportedAction() {
        return SetNullableAction.class;
    }

    @Override
    public ValidationErrors validate(SetNullableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("columnName", action)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredContainer("Table name is required", "columnName", action);
    }

    @Override
    public ActionResult execute(SetNullableAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(SetNullableAction action, Scope scope) {
        if (ObjectUtil.defaultIfEmpty(action.nullable, false)) {
            return new StringClauses().append("NULL");
        } else {
            return new StringClauses().append("NOT NULL");
        }
    }
}
