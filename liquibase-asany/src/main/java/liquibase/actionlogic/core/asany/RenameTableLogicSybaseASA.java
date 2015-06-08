package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.RenameTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;

public class RenameTableLogicSybaseASA extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameTableAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME TO", "RENAME");
    }
}
