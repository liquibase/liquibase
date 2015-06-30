package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.util.StringClauses;

public class RenameColumnLogicH2 extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME COLUMN", "ALTER COLUMN")
                .replace("TO", "RENAME TO");
    }
}
