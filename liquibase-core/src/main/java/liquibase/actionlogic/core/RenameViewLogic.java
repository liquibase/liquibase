package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.View;

public class RenameViewLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RenameViewAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RenameViewAction.Attr.oldViewName, action)
                .checkForRequiredField(RenameViewAction.Attr.newViewName, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("RENAME")
                .append(database.escapeObjectName(action.get(RenameViewAction.Attr.oldViewName, ObjectName.class), View.class))
                .append("TO")
                .append(database.escapeObjectName(action.get(RenameViewAction.Attr.newViewName, ObjectName.class).getName(), View.class));
    }
}
