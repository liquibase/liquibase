package liquibase.actionlogic.core.derby;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropDefaultValueAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.derby.DerbyDatabase;

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
