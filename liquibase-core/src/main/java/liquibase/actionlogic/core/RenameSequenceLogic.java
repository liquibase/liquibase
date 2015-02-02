package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.RenameSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Sequence;

public class RenameSequenceLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return RenameSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class).supportsSequences();
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField(RenameSequenceAction.Attr.newSequenceName, action)
                .checkForRequiredField(RenameSequenceAction.Attr.oldSequenceName, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("ALTER SEQUENCE")
                .append(database.escapeSequenceName(action.get(RenameSequenceAction.Attr.catalogName, String.class),
                        action.get(RenameSequenceAction.Attr.schemaName, String.class),
                        action.get(RenameSequenceAction.Attr.oldSequenceName, String.class)))
                .append("RENAME TO")
                .append(database.escapeObjectName(action.get(RenameSequenceAction.Attr.newSequenceName, String.class), Sequence.class));
    }
}
