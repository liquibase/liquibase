package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.DropForeignKeyAction;
import liquibase.actionlogic.core.DropForeignKeyLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.util.StringClauses;

public class DropForeignKeyLogicMysql extends DropForeignKeyLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropForeignKeyAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP FOREIGN KEY");
    }
}
