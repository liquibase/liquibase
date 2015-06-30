package liquibase.actionlogic.core;

import java.util.ArrayList;
import java.util.List;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropColumnsAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

public class DropColumnsLogic extends AbstractActionLogic<DropColumnsAction> {

    @Override
    protected Class<DropColumnsAction> getSupportedAction() {
        return DropColumnsAction.class;
    }

    @Override
    public ValidationErrors validate(DropColumnsAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.checkForRequiredField("tableName", action);
        errors.checkForRequiredField("columnNames", action);
        return errors;
    }

    @Override
    public ActionResult execute(DropColumnsAction action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();
        for (String column : CollectionUtil.createIfNull(action.columnNames)) {
            actions.add(new ExecuteSqlAction(generateDropSql(column, action, scope).toString()));
        }
        return new DelegateResult(actions);
    }

    protected StringClauses generateDropSql(String column, DropColumnsAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses()
                .append("ALTER TABLE " + database.escapeObjectName(action.tableName, Table.class))
                        .append("DROP COLUMN")
                        .append(database.escapeObjectName(column, Column.class));
    }
}
