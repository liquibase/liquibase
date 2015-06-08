package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropForeignKeyConstraintAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropForeignKeyConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;

public class DropForeignKeyConstraintLogicMysql extends DropForeignKeyConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyConstraintAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP FOREIGN KEY");
    }
}
