package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;
import liquibase.util.StringClauses;

public class DropDefaultValueLogicDerby extends DropDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return DerbyDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropDefaultValueAction action, Scope scope) {
        return new StringClauses().append("WITH DEFAULT NULL");
    }
}
