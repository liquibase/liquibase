package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.RenameViewAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.View;
import liquibase.util.StringClauses;

public class RenameViewLogic extends AbstractSqlBuilderLogic<RenameViewAction> {

    @Override
    protected Class<RenameViewAction> getSupportedAction() {
        return RenameViewAction.class;
    }

    @Override
    public ValidationErrors validate(RenameViewAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("oldViewName", action)
                .checkForRequiredField("newViewName", action);
    }

    @Override
    protected StringClauses generateSql(RenameViewAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("RENAME")
                .append(database.escapeObjectName(action.oldViewName, View.class))
                .append("TO")
                .append(database.escapeObjectName(action.newViewName.name, View.class));
    }
}
