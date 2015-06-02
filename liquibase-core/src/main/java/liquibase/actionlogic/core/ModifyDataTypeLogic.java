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

public class ModifyDataTypeLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        dataType
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return ModifyDataTypeAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(ModifyDataTypeAction.Attr.columnName, action)
                .checkForRequiredContainer("Table name is required", ModifyDataTypeAction.Attr.columnName, action)
                .checkForRequiredField(ModifyDataTypeAction.Attr.newDataType, action);
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterColumnAction(
                action.get(ModifyDataTypeAction.Attr.columnName, ObjectName.class),
                generateSql(action, scope)
        ));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new StringClauses()
                .append(Clauses.dataType, DataTypeFactory.getInstance().fromDescription(action.get(ModifyDataTypeAction.Attr.newDataType, String.class), database).toDatabaseDataType(database).toSql());
    }
}
