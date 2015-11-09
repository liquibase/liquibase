package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;
import liquibase.util.StringClauses;

public class AlterColumnLogic extends AbstractActionLogic<AlterColumnAction> {

    public static enum Clauses {
        tableName,
        columnName,
        newDefinition,
    }

    @Override
    protected Class<AlterColumnAction> getSupportedAction() {
        return AlterColumnAction.class;
    }

    @Override
    public ValidationErrors validate(AlterColumnAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("columnName", action)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredField("newDefinition", action);
    }

    @Override
    public ActionResult execute(AlterColumnAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(getAlterColumnClauses(action, scope)));
    }

    protected StringClauses getAlterColumnClauses(AlterColumnAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses(" ")
                .append("ALTER TABLE")
                .append(Clauses.tableName, database.escapeObjectName(action.columnName.container, Table.class))
                .append("ALTER COLUMN")
                .append(Clauses.columnName, database.escapeObjectName(action.columnName.name, Column.class))
                .append(Clauses.newDefinition, action.newDefinition.toString().trim());
    }
}
