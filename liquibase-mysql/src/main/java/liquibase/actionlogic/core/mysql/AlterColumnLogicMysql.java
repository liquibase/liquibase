package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.AlterColumnAction;
import liquibase.actionlogic.core.AlterColumnLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.util.StringClauses;

public class AlterColumnLogicMysql extends AlterColumnLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }


    @Override
    protected StringClauses getAlterColumnClauses(AlterColumnAction action, Scope scope) {
        return super.getAlterColumnClauses(action, scope)
                .replace("ALTER COLUMN", "MODIFY");
    }
}
