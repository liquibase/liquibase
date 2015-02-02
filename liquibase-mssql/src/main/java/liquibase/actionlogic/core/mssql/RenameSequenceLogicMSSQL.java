package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.RenameSequenceLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.structure.core.Sequence;

public class RenameSequenceLogicMSSQL extends RenameSequenceLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        return new StringClauses()
                .append("SP_RENAME ")
                .append(database.escapeObjectName(action.get(RenameSequenceAction.Attr.oldSequenceName, String.class), Sequence.class))
                .append(", ")
                .append(database.escapeObjectName(action.get(RenameSequenceAction.Attr.newSequenceName, String.class), Sequence.class));
    }
}
