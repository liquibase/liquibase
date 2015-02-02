package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.action.core.RedefineColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class DropDefaultValueLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropDefaultValueAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(DropDefaultValueAction.Attr.tableName, action)
                .checkForRequiredField(DropDefaultValueAction.Attr.columnName, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new RedefineColumnAction(
                action.get(DropDefaultValueAction.Attr.catalogName, String.class),
                action.get(DropDefaultValueAction.Attr.schemaName, String.class),
                action.get(DropDefaultValueAction.Attr.tableName, String.class),
                action.get(DropDefaultValueAction.Attr.columnName, String.class),
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return new StringClauses().append("DEFAULT NULL");
    }
}
