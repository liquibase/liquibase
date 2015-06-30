package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropSequenceAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Sequence;
import liquibase.util.StringClauses;

public class DropSequenceLogic extends AbstractSqlBuilderLogic<DropSequenceAction> {

    @Override
    protected Class<DropSequenceAction> getSupportedAction() {
        return DropSequenceAction.class;
    }


    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(DropSequenceAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("sequenceName", action);
    }

    @Override
    protected StringClauses generateSql(DropSequenceAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP SEQUENCE")
                .append(database.escapeObjectName(action.sequenceName, Sequence.class));
    }
}
