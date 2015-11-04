package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.actionlogic.core.AddPrimaryKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.structure.core.PrimaryKey;
import liquibase.util.StringClauses;

public class AddPrimaryKeysLogicMysql extends AddPrimaryKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        StringClauses clauses = super.generateSql(pk, action, scope);
//        clauses.replace("CONSTRAINT", "PRIMARY KEY");

        return clauses;
    }
}
