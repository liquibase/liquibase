package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.DropForeignKeyAction;
import liquibase.actionlogic.core.DropForeignKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.util.StringClauses;

public class DropForeignKeyLogicSybaseASA extends DropForeignKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP FOREIGN KEY");
    }
}
