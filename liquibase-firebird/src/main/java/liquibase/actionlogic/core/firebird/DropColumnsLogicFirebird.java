package liquibase.actionlogic.core.firebird;

import liquibase.Scope;
import liquibase.action.core.DropColumnsAction;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;
import liquibase.util.StringClauses;

public class DropColumnsLogicFirebird extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return FirebirdDatabase.class;
    }

    @Override
    protected StringClauses generateDropSql(String column, DropColumnsAction action, Scope scope) {
        return super.generateDropSql(column, action, scope)
                .replace("DROP COLUMN", "DROP");
    }
}
