package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ModifyDataTypeAction;
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

public class ModifyDataTypeLogic extends AbstractSqlBuilderLogic<ModifyDataTypeAction> {

    public static enum Clauses {
        dataType
    }

    @Override
    protected Class<ModifyDataTypeAction> getSupportedAction() {
        return ModifyDataTypeAction.class;
    }

    @Override
    public ValidationErrors validate(ModifyDataTypeAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("columnName", action)
                .checkForRequiredContainer("Table name is required", "columnName", action)
                .checkForRequiredField("newDataType", action);
    }

    @Override
    public ActionResult execute(ModifyDataTypeAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.columnName,
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(ModifyDataTypeAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses()
                .append(Clauses.dataType, DataTypeFactory.getInstance().fromDescription(action.newDataType, database).toDatabaseDataType(database).toSql());
    }
}
