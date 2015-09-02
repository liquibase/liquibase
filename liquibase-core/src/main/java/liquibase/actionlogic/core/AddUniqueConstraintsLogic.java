package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddUniqueConstraintsAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddUniqueConstraintsLogic extends AbstractActionLogic<AddUniqueConstraintsAction> {

    public static enum Clauses {
        constraintName,
        tablespace
    }

    @Override
    protected Class<AddUniqueConstraintsAction> getSupportedAction() {
        return AddUniqueConstraintsAction.class;
    }

    @Override
    public ValidationErrors validate(AddUniqueConstraintsAction action, Scope scope) {
        ValidationErrors validationErrors = super.validate(action, scope);
        for (UniqueConstraint uq : action.uniqueConstraints) {
            validationErrors.checkForRequiredField("columns", uq);
            ObjectName tableName = uq.name.container;
            if (tableName == null) {
                validationErrors.addError("No table specified for unique constraint "+uq.name);
            }
            for (ObjectName columnName : uq.columns) {
                if (columnName.container == null) {
                    validationErrors.addError("Column "+columnName.name+" must have a table");
                    if (!tableName.equals(columnName.container)) {
                        validationErrors.addError("All columns must be in the same table");
                    }
                }
            }
        }
        return validationErrors;
    }


    @Override
    public ActionResult execute(AddUniqueConstraintsAction action, Scope scope) throws ActionPerformException {

        List<Action> actions = new ArrayList<>();

        for (UniqueConstraint uq : action.uniqueConstraints) {
            actions.addAll(Arrays.asList(execute(uq, action, scope)));
        }

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action execute(UniqueConstraint uq, AddUniqueConstraintsAction action, Scope scope) {
        return new AlterTableAction(
                uq.columns.get(0).container,
                generateSql(uq, action, scope)
        );
    }

    protected StringClauses generateSql(UniqueConstraint uniqueConstraint, AddUniqueConstraintsAction action, Scope scope) {
        String constraintName = uniqueConstraint.name.name;
        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses();
        clauses.append("ADD CONSTRAINT");
        if (constraintName != null) {
            clauses.append(Clauses.constraintName, database.escapeObjectName(constraintName, Index.class));
        }
        clauses.append("UNIQUE");
        clauses.append("(" + (StringUtils.join(uniqueConstraint.columns, ", ", new StringUtils.ObjectNameFormatter(Column.class, scope.getDatabase()))) + ")");

        if (database.supportsInitiallyDeferrableColumns()) {
            if (ObjectUtil.defaultIfEmpty(uniqueConstraint.deferrable, false)) {
                clauses.append("DEFERRABLE");
            }

            if (ObjectUtil.defaultIfEmpty(uniqueConstraint.initiallyDeferred, false)) {
                clauses.append("INITIALLY DEFERRED");
            }
        }

        if (ObjectUtil.defaultIfEmpty(uniqueConstraint.disabled, false)) {
            clauses.append("DISABLE");
        }

        String tablespace = uniqueConstraint.tablespace;

        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;

    }
}
