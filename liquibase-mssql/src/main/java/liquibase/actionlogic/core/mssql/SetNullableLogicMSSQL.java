package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.core.SetNullableLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;
import liquibase.exception.ValidationErrors;
import liquibase.util.StringClauses;

public class SetNullableLogicMSSQL extends SetNullableLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(SetNullableAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("columnDataType", action);
    }

    @Override
    protected StringClauses generateSql(SetNullableAction action, Scope scope) {
        Database database = scope.getDatabase();
        return super.generateSql(action, scope)
                .prepend(DataTypeFactory.getInstance().fromDescription(action.columnDataType, database).toDatabaseDataType(database).toSql());
    }

}
