package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class AddAutoIncrementLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        autoIncrementDefinition, dataType
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class).supportsAutoIncrement();
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.columnName, action);
        validationErrors.checkForRequiredField(AddAutoIncrementAction.Attr.columnDataType, action);

        if (!validationErrors.hasErrors()) {
            if (action.get(AddAutoIncrementAction.Attr.columnName, ObjectName.class).asList().size() < 2) {
                validationErrors.addError("Table name is required");
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.get(AddAutoIncrementAction.Attr.columnName, ObjectName.class),
                generateSql(action, scope)));
    }

    protected StringClauses generateSql(Action action, Scope scope) {

        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = new StringClauses();
        clauses.append(Clauses.dataType, DataTypeFactory.getInstance().fromDescription(action.get(AddAutoIncrementAction.Attr.columnDataType, String.class) + "{autoIncrement:true}", database).toDatabaseDataType(database).toSql());
        clauses.append(Clauses.autoIncrementDefinition, database.getAutoIncrementClause(action.get(AddAutoIncrementAction.Attr.startWith, BigInteger.class), action.get(AddAutoIncrementAction.Attr.incrementBy, BigInteger.class)));

        return clauses;
    }
}
