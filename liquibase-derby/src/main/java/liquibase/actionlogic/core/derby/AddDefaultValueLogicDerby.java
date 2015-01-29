package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
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
    protected String generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        return "WITH DEFAULT " + DataTypeFactory.getInstance().fromObject(defaultValue, database).objectToSql(defaultValue, database);
    }
}