package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.StoredProcedureAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class StoredProcedureLogic extends AbstractSqlBuilderLogic<StoredProcedureAction> {

    @Override
    protected Class<StoredProcedureAction> getSupportedAction() {
        return StoredProcedureAction.class;
    }

    @Override
    public ValidationErrors validate(StoredProcedureAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("procedureName", action);
    }

    @Override
    protected StringClauses generateSql(StoredProcedureAction action, Scope scope) {
        return new StringClauses()
                .append("EXEC")
                .append(scope.getDatabase().escapeObjectName(action.procedureName, StoredProcedure.class))
                .append("(" + StringUtils.join(CollectionUtil.createIfNull(action.parameterNames), ", ") + ")");

    }
}
