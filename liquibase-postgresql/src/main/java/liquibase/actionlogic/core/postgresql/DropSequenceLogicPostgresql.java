package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.core.DropSequenceAction;
import liquibase.actionlogic.core.DropSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.util.StringClauses;

public class DropSequenceLogicPostgresql extends DropSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropSequenceAction action, Scope scope) {
        return super.generateSql(action, scope)
                .append("CASCADE");
    }
}
