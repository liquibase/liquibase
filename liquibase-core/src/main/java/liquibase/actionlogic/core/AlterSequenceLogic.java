package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AlterSequenceAction;
import liquibase.action.core.RedefineSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

import java.math.BigInteger;

public class AlterSequenceLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        incrementBy,
        minValue,
        maxValue
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AlterSequenceAction.class;
    }

    @Override
    protected boolean supportsScope(Scope scope) {
        return super.supportsScope(scope) && scope.get(Scope.Attr.database, Database.class).supportsSequences();
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();
        validationErrors.checkForRequiredField(AlterSequenceAction.Attr.sequenceName, action);

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        return new RewriteResult(new RedefineSequenceAction(
                action.get(AlterSequenceAction.Attr.catalogName, String.class),
                action.get(AlterSequenceAction.Attr.schemaName, String.class),
                action.get(AlterSequenceAction.Attr.sequenceName, String.class),
                generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = new StringClauses();

        BigInteger incrementBy = action.get(AlterSequenceAction.Attr.incrementBy, BigInteger.class);
        BigInteger minValue = action.get(AlterSequenceAction.Attr.minValue, BigInteger.class);
        BigInteger maxValue = action.get(AlterSequenceAction.Attr.maxValue, BigInteger.class);
        boolean ordered = action.get(AlterSequenceAction.Attr.ordered, false);

        if (incrementBy != null) {
                clauses.append(Clauses.incrementBy, "INCREMENT BY " + incrementBy);
        }

        if (minValue != null) {
            clauses.append(Clauses.minValue, "RESTART WITH "+minValue);
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
