package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterSequenceAction;
import liquibase.action.core.RedefineSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.util.ObjectUtil;

import java.math.BigInteger;

public class AlterSequenceLogic extends AbstractSqlBuilderLogic<AlterSequenceAction> {

    public static enum Clauses {
        incrementBy,
        minValue,
        maxValue
    }

    @Override
    protected Class<AlterSequenceAction> getSupportedAction() {
        return AlterSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.getDatabase().supportsSequences();
    }

    @Override
    public ValidationErrors validate(AlterSequenceAction action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField("sequenceName", action);

        return validationErrors;
    }

    @Override
    public ActionResult execute(AlterSequenceAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new RedefineSequenceAction(
                action.sequenceName,
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(AlterSequenceAction action, Scope scope) {
        StringClauses clauses = new StringClauses();

        BigInteger incrementBy = action.incrementBy;
        BigInteger minValue = action.minValue;
        BigInteger maxValue = action.maxValue;
        boolean ordered = ObjectUtil.defaultIfEmpty(action.ordered, false);

        if (incrementBy != null) {
            clauses.append(Clauses.incrementBy, "INCREMENT BY " + incrementBy);
        }

        if (minValue != null) {
            clauses.append(Clauses.minValue, "RESTART WITH " + minValue);
        }

        if (maxValue != null) {
            clauses.append(Clauses.maxValue, "MAXVALUE " + minValue);
        }

        if (ordered) {
            clauses.append("ORDER");
        }

        return clauses;
    }
}
