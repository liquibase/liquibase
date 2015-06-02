package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Sequence;

public class CreateSequenceLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        sequenceName, startWith, incrementBy, maxValue, minValue,
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class).supportsSequences();
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForRequiredField(CreateSequenceAction.Attr.sequenceName, action);
        errors.checkForDisallowedField(CreateSequenceAction.Attr.cacheSize, action, scope.get(Scope.Attr.database, Database.class).getShortName());

        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String incrementBy = action.get(CreateSequenceAction.Attr.incrementBy, String.class);
        String minValue = action.get(CreateSequenceAction.Attr.minValue, String.class);
        String maxValue = action.get(CreateSequenceAction.Attr.maxValue, String.class);
        String startValue = action.get(CreateSequenceAction.Attr.startValue, String.class);
        boolean cycle = action.get(CreateSequenceAction.Attr.cycle, false);

        StringClauses clauses = new StringClauses();

        clauses.append("CREATE SEQUENCE");
        clauses.append(database.escapeObjectName(action.get(CreateSequenceAction.Attr.sequenceName, ObjectName.class), Sequence.class));

        clauses.append(Clauses.startWith, startValue == null?null:"START WITH " + startValue);
        clauses.append(Clauses.incrementBy, incrementBy == null?null:"INCREMENT BY " + incrementBy);
        clauses.append(Clauses.minValue, minValue == null?null:"MINVALUE " + minValue);
        clauses.append(Clauses.maxValue, maxValue == null?null:"MAXVALUE " + maxValue);
        clauses.append(Clauses.maxValue, maxValue == null?null:"MAXVALUE " + maxValue);


        if (cycle) {
            clauses.append("CYCLE");
        }

        return clauses;
    }
}
