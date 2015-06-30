package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.util.StringClauses;

public class AddPrimaryKeyLogicMysql extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(AddPrimaryKeyAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        clauses.replace("ADD CONSTRAINT", "ADD PRIMARY KEY");
        clauses.remove(Clauses.constraintName);

        return clauses;
    }
}
