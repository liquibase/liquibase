package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;

public class DropColumnsLogicInformix extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateDropSql(String column, Action action, Scope scope) {
        return super.generateDropSql(column, action, scope)
                .replace("DROP COLUMN", "DROP");
    }
}
