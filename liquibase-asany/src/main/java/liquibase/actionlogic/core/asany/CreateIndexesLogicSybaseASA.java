package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.CreateIndexesAction;
import liquibase.actionlogic.core.CreateIndexesLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.structure.core.Index;
import liquibase.util.StringClauses;

public class CreateIndexesLogicSybaseASA extends CreateIndexesLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    protected StringClauses generateSql(Index index, CreateIndexesAction action, Scope scope) {
        StringClauses clauses = super.generateSql(index, action, scope);

        String tablespace = index.tablespace;
        if (tablespace != null) {
            clauses.replace(tablespace, "ON "+tablespace);
        }

        return clauses;
    }
}
