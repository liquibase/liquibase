package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.SetNullableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.SetNullableLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;

public class SetNullableLogicMSSQL extends SetNullableLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(SetNullableAction.Attr.columnDataType, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return super.generateSql(action, scope)
                .prepend(DataTypeFactory.getInstance().fromDescription(action.get(SetNullableAction.Attr.columnDataType, String.class), database).toDatabaseDataType(database).toSql());
    }

}
