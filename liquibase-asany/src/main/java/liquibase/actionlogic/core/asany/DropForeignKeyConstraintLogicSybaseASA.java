package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropForeignKeyConstraintAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;

public class DropForeignKeyConstraintLogicSybaseASA extends DropForeignKeyConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyConstraintAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP FOREIGN KEY");
    }
}
