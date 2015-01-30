package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;

public class DropSequenceLogicPostgresql extends DropSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .append("CASCADE");
    }
}
