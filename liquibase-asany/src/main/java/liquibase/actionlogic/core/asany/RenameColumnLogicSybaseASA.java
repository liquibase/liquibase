package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.util.StringClauses;

public class RenameColumnLogicSybaseASA extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME COLUMN", "RENAME");
    }
}
