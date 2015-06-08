package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.datatype.DataTypeFactory;

public class AddDefaultValueLogicDerby extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddDefaultValueAction action, Scope scope) {
        Database database = scope.getDatabase();
        Object defaultValue = action.defaultValue;

        return new StringClauses().append("WITH DEFAULT").append(DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database));
    }
}