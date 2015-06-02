package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.AlterColumnAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.BooleanType;
import liquibase.datatype.core.CharType;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.ObjectName;

public class AddDefaultValueLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddDefaultValueAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        Database database = scope.get(Scope.Attr.database, Database.class);

        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField(AddDefaultValueAction.Attr.defaultValue, action);
        validationErrors.checkForRequiredField(AddDefaultValueAction.Attr.columnName, action);
        validationErrors.checkForRequiredContainer("Table name is required", AddDefaultValueAction.Attr.columnName, action);
        if (!database.supportsSequences() && defaultValue instanceof SequenceNextValueFunction) {
            validationErrors.addError("Database " + database.getShortName() + " does not support sequences");
        }

        String columnDataType = action.get(AddDefaultValueAction.Attr.columnDataType, String.class);
        if (columnDataType != null) {
            LiquibaseDataType dataType = DataTypeFactory.getInstance().fromDescription(columnDataType, database);
            boolean typeMismatch = false;
            if (dataType instanceof BooleanType) {
                if (!(defaultValue instanceof Boolean)) {
                    typeMismatch = true;
                }
            } else if (dataType instanceof CharType) {
                if (!(defaultValue instanceof String)) {
                    typeMismatch = true;
                }
            }

            if (typeMismatch) {
                validationErrors.addError("Default value of " + defaultValue + " does not match defined type of " + columnDataType);
            }
        }

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.get(AddDefaultValueAction.Attr.columnName, ObjectName.class),
                generateSql(action, scope)
        ));
    }

    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        return new StringClauses().append("DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database));
    }
}
