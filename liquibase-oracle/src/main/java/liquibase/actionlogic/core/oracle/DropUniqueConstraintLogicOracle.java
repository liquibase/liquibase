package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.actionlogic.core.DropUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.util.StringClauses;

public class DropUniqueConstraintLogicOracle extends DropUniqueConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(DropUniqueConstraintActon action, Scope scope) {
        return super.generateSql(action, scope)
                .append("DROP INDEX");
    }
}
