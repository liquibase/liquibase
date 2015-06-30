package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.DropColumnsAction;
import liquibase.actionlogic.core.DropColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.util.StringClauses;

public class DropColumnsLogicSybaseASA extends DropColumnsLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateDropSql(String column, DropColumnsAction action, Scope scope) {
        return super.generateDropSql(column, action, scope)
                .replace("DROP COLUMN", "DROP");
    }
}
