package liquibase.actionlogic.core.db2;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.database.core.db2.DB2Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

import java.math.BigInteger;

public class AddAutoIncrementLogicDB2 extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected int getPriority() {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class) instanceof DB2Database;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.columnName, action);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.tableName, action);

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new RewriteResult(new ExecuteSqlAction("ALTER TABLE "
                + database.escapeTableName(action.get(AddAutoIncrementAction.Attr.catalogName, String.class), action.get(AddAutoIncrementAction.Attr.schemaName, String.class), action.get(AddAutoIncrementAction.Attr.tableName, String.class))
                + " ALTER COLUMN "
                + database.escapeColumnName(
                action.get(AddAutoIncrementAction.Attr.catalogName, String.class),
                action.get(AddAutoIncrementAction.Attr.schemaName, String.class),
                action.get(AddAutoIncrementAction.Attr.tableName, String.class),
                action.get(AddAutoIncrementAction.Attr.columnName, String.class))
                + " SET "
                + database.getAutoIncrementClause(
                action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class), action.get(AddAutoIncrementAction.Attr.incrementBy, BigInteger.class))
        )
        );
    }
}