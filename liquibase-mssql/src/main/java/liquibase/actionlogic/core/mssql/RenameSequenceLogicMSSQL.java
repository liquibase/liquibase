package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.core.RenameSequenceAction;
import liquibase.actionlogic.core.RenameSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.Sequence;
import liquibase.util.StringClauses;

public class RenameSequenceLogicMSSQL extends RenameSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(RenameSequenceAction action, Scope scope) {
        Database database = scope.getDatabase();

        return new StringClauses()
                .append("SP_RENAME ")
                .append(database.escapeObjectName(action.oldSequenceName, Sequence.class))
                .append(", ")
                .append(database.escapeObjectName(action.newSequenceName.name, Sequence.class));
    }
}
