package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

import java.math.BigInteger;

public class AddAutoIncrementLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class).supportsAutoIncrement();
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.columnName, action);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.tableName, action);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.columnDataType, action);

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String newDefinition = DataTypeFactory.getInstance().fromDescription(action.get(AddAutoIncrementAction.Attr.columnDataType, String.class) + "{autoIncrement:true}", database).toDatabaseDataType(database)
                + " "
                + database.getAutoIncrementClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class), action.get(AddAutoIncrementAction.Attr.incrementBy, BigInteger.class));

        return new RewriteResult(new AlterColumnAction(
                action.get(AddAutoIncrementAction.Attr.catalogName, String.class),
                action.get(AddAutoIncrementAction.Attr.schemaName, String.class),
                action.get(AddAutoIncrementAction.Attr.tableName, String.class),
                action.get(AddAutoIncrementAction.Attr.columnName, String.class),
                newDefinition));
    }
}
