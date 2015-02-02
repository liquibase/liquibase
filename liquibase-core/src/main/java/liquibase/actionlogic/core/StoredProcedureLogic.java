package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StoredProcedureAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringUtils;

public class StoredProcedureLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return StoredProcedureAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(StoredProcedureAction.Attr.procedureName, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return new StringClauses()
                .append("EXEC")
                .append(action.get(StoredProcedureAction.Attr.procedureName, String.class))
                .append("(" + StringUtils.join(action.get(StoredProcedureAction.Attr.parameterNames, new String[0]), ", ") + ")");

    }
}
