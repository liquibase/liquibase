package liquibase.actionlogic.core.firebird;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;
import liquibase.util.StringClauses;

public class RenameColumnLogicFirebird extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return FirebirdDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME COLUMN", "ALTER COLUMN");
    }
}
