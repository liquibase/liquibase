package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.actionlogic.core.DropUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;
import liquibase.util.StringClauses;

public class DropUniqueConstraintLogicMysql extends DropUniqueConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropUniqueConstraintActon action, Scope scope) {
        return super.generateSql(action, scope)
                .replace("DROP CONSTRAINT", "DROP KEY");
    }
}
