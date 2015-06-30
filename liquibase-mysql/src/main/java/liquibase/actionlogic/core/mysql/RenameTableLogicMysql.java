package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.RenameTableAction;
import liquibase.actionlogic.core.RenameTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.util.StringClauses;

public class RenameTableLogicMysql extends RenameTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameTableAction action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("RENAME", "RENAME TABLE");
    }
}
