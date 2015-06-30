package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.AlterColumnAction;
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
import liquibase.util.StringClauses;

public class AddDefaultValueLogic extends AbstractSqlBuilderLogic<AddDefaultValueAction> {

    @Override
    protected Class<AddDefaultValueAction> getSupportedAction() {
        return AddDefaultValueAction.class;
    }

    @Override
    public ValidationErrors validate(AddDefaultValueAction action, Scope scope) {
        Object defaultValue = action.defaultValue;

        Database database = scope.getDatabase();

        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField("defaultValue", action);
        validationErrors.checkForRequiredField("columnName", action);
        validationErrors.checkForRequiredContainer("Table name is required", "columnName", action);
        if (!database.supportsSequences() && defaultValue instanceof SequenceNextValueFunction) {
            validationErrors.addError("Database " + database.getShortName() + " does not support sequences");
        }

        String columnDataType = action.columnDataType;
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
    public ActionResult execute(AddDefaultValueAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)
        ));
    }

    protected StringClauses generateSql(AddDefaultValueAction action, Scope scope) {
        Database database = scope.getDatabase();
        Object defaultValue = action.defaultValue;

        return new StringClauses().append("DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database));
    }
}
