package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameViewLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;

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
