package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;

public class DropUniqueConstraintLogicMysql extends DropUniqueConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP KEY");
    }
}
