package liquibase.actionlogic.core.firebird;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;

public class DropColumnsLogicFirebird extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return FirebirdDatabase.class;
    }

    @Override
    protected StringClauses generateDropSql(String column, Action action, Scope scope) {
        return super.generateDropSql(column, action, scope)
                .replace("DROP COLUMN", "DROP");
    }
}
