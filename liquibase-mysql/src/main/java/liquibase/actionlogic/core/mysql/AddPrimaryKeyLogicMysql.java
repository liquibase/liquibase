package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.AddPrimaryKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;

public class AddPrimaryKeyLogicMysql extends AddPrimaryKeyLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);
        clauses.replace("ADD CONSTRAINT", "ADD PRIMARY KEY");
        clauses.remove(Clauses.constraintName);

        return clauses;
    }
}
