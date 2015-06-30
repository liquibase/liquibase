package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.RenameSequenceAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Sequence;
import liquibase.util.StringClauses;

public class RenameSequenceLogic extends AbstractSqlBuilderLogic<RenameSequenceAction> {

    @Override
    protected Class<RenameSequenceAction> getSupportedAction() {
        return RenameSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(RenameSequenceAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("newSequenceName", action)
                .checkForRequiredField("oldSequenceName", action);
    }

    @Override
    protected StringClauses generateSql(RenameSequenceAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("ALTER SEQUENCE")
                .append(database.escapeObjectName(action.oldSequenceName, Sequence.class))
                .append("RENAME TO")
                .append(database.escapeObjectName(action.newSequenceName, Sequence.class));
    }
}
