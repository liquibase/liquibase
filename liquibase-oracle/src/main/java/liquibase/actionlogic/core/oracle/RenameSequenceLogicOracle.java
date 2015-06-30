package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.RenameSequenceAction;
import liquibase.actionlogic.core.RenameSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.structure.core.Sequence;
import liquibase.util.StringClauses;

public class RenameSequenceLogicOracle extends RenameSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameSequenceAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses()
                .append("RENAME")
                .append(database.escapeObjectName(action.oldSequenceName, Sequence.class))
                .append("TO")
                .append(database.escapeObjectName(action.newSequenceName, Sequence.class));
    }
}
