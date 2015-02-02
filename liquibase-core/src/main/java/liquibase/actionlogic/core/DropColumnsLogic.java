package liquibase.actionlogic.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropColumnsAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;

public class DropColumnsLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropColumnsAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.checkForRequiredField(DropColumnsAction.Attr.tableName, action);
        errors.checkForRequiredField(DropColumnsAction.Attr.columnNames, action);
        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();
        for (String column : action.get(DropColumnsAction.Attr.columnNames, String[].class)) {
            actions.add(new ExecuteSqlAction(generateDropSql(column, action, scope).toString()));
        }
        return new DelegateResult(actions);
    }

    protected StringClauses generateDropSql(String column, Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new StringClauses()
                .append("ALTER TABLE " + database.escapeTableName(action.get(DropColumnsAction.Attr.catalogName, String.class), action.get(DropColumnsAction.Attr.schemaName, String.class), action.get(DropColumnsAction.Attr.tableName, String.class)))
                .append("DROP COLUMN")
                .append(database.escapeObjectName(column, Column.class));
    }
}
