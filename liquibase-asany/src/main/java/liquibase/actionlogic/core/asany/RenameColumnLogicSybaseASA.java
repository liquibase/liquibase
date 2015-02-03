package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;

public class RenameColumnLogicSybaseASA extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME COLUMN", "RENAME");
    }
}
