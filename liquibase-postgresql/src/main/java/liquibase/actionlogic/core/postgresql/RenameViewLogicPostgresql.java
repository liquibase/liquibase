package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.core.RenameViewAction;
import liquibase.actionlogic.core.RenameViewLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.util.StringClauses;

public class RenameViewLogicPostgresql extends RenameViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameViewAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME", "ALTER TABLE")
                .replace("TO", "RENAME TO");
    }
}
