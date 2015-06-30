package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateSequenceAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Sequence;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

import java.math.BigInteger;

public class CreateSequenceLogic extends AbstractSqlBuilderLogic<CreateSequenceAction> {

    public static enum Clauses {
        sequenceName, startWith, incrementBy, maxValue, minValue,
    }

    @Override
    protected Class<CreateSequenceAction> getSupportedAction() {
        return CreateSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(CreateSequenceAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);

        errors.checkForRequiredField("sequenceName", action);
        errors.checkForDisallowedField("cacheSize", action, scope.getDatabase().getShortName());

        return errors;
    }

    @Override
    public ActionResult execute(CreateSequenceAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
    }

    @Override
    protected StringClauses generateSql(CreateSequenceAction action, Scope scope) {
        Database database = scope.getDatabase();
        BigInteger incrementBy = action.incrementBy;
        BigInteger minValue = action.minValue;
        BigInteger maxValue = action.maxValue;
        BigInteger startValue = action.startValue;
        boolean cycle = ObjectUtil.defaultIfEmpty(action.cycle, false);

        StringClauses clauses = new StringClauses();

        clauses.append("CREATE SEQUENCE");
        clauses.append(database.escapeObjectName(action.sequenceName, Sequence.class));

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
