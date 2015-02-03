package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.datatype.DataTypeFactory;

public class AddDefaultValueLogicMSSQL extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        MSSQLDatabase database = scope.get(Scope.Attr.database, MSSQLDatabase.class);
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        return new StringClauses()
                .append("ADD CONSTRAINT")
                .append(database.generateDefaultConstraintName(action.get(AddDefaultValueAction.Attr.tableName, String.class), action.get(AddDefaultValueAction.Attr.columnName, String.class)))
                .append("DEFAULT")
                .append(DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database))
                .append("FOR")
                .append(action.get(AddDefaultValueAction.Attr.columnName, String.class));
    }
}