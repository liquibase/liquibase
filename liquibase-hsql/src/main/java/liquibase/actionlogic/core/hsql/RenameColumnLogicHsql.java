package liquibase.actionlogic.core.hsql;

import liquibase.Scope;
import liquibase.action.core.RenameColumnAction;
import liquibase.actionlogic.core.RenameColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.hsql.HsqlDatabase;
import liquibase.util.StringClauses;

public class RenameColumnLogicHsql extends RenameColumnLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return HsqlDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameColumnAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME COLUMN", "ALTER COLUMN")
                .replace("TO", "RENAME TO");
    }
}
