package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.AddUniqueConstraintAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class AddUniqueConstraintLogic extends AbstractSqlBuilderLogic<AddUniqueConstraintAction> {

    public static enum Clauses {
        constraintName,
        tablespace
    }

    @Override
    protected Class<AddUniqueConstraintAction> getSupportedAction() {
        return AddUniqueConstraintAction.class;
    }

    @Override
    public ValidationErrors validate(AddUniqueConstraintAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        validationErrors.checkForRequiredField("tableName", action);
        validationErrors.checkForRequiredField("columnNames", action);
        return validationErrors;
    }

    @Override
    public ActionResult execute(AddUniqueConstraintAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(new AlterTableAction(
                action.tableName,
                generateSql(action, scope)
        ));
    }

    protected StringClauses generateSql(AddUniqueConstraintAction action, Scope scope) {
        String constraintName = action.constraintName;
        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses();
        clauses.append("ADD CONSTRAINT");
        if (constraintName != null) {
            clauses.append(Clauses.constraintName, database.escapeConstraintName(constraintName));
        }
        clauses.append("UNIQUE");
        clauses.append("(" + database.escapeColumnNameList(StringUtils.join(action.columnNames, ",")) + "");

        if (database.supportsInitiallyDeferrableColumns()) {
            if (ObjectUtil.defaultIfEmpty(action.deferrable, false)) {
                clauses.append("DEFERRABLE");
            }

            if (ObjectUtil.defaultIfEmpty(action.initiallyDeferred, false)) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        if (ObjectUtil.defaultIfEmpty(action.disabled, false)) {
            clauses.append("DISABLE");
        }

        String tablespace = action.tablespace;

        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;

    }
}
