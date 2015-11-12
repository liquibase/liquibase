package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropViewAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

public class DropViewLogic extends AbstractSqlBuilderLogic<DropViewAction> {

    @Override
    protected Class<DropViewAction> getSupportedAction() {
        return DropViewAction.class;
    }

    @Override
    public ValidationErrors validate(DropViewAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("viewName", action);
    }

    @Override
    protected StringClauses generateSql(DropViewAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP VIEW")
                .append(database.escapeObjectName(action.viewName
                ));
    }
}
