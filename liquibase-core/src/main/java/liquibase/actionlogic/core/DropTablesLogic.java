package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropTablesAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

import java.util.ArrayList;
import java.util.List;

public class DropTablesLogic extends AbstractActionLogic<DropTablesAction> {

    @Override
    protected Class<DropTablesAction> getSupportedAction() {
        return DropTablesAction.class;
    }

    @Override
    public ValidationErrors validate(DropTablesAction action, Scope scope) {
        Database database = scope.getDatabase();

        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("tableNames", action);

        if (ObjectUtil.defaultIfEmpty(action.cascadeConstraints, false) && !supportsDropTableCascadeConstraints()) {
            errors.checkForDisallowedField("cascadeConstraints", action, database.getShortName());
        }
        return errors;
    }

    protected boolean supportsDropTableCascadeConstraints() {
        return true;
    }

    @Override
    public ActionResult execute(DropTablesAction action, Scope scope) throws ActionPerformException {
        List<Action> actions = new ArrayList<>();

        for (ObjectReference tableName: action.tableNames) {
            actions.add(new ExecuteSqlAction(generateSql(tableName, action, scope)));
        }

        return new DelegateResult(actions);
    }

    protected StringClauses generateSql(ObjectReference tableName, DropTablesAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses()
                .append("DROP TABLE")
                .append(database.escapeObjectName(tableName, Table.class));

        if (ObjectUtil.defaultIfEmpty(action.cascadeConstraints, false)) {
            clauses.append("CASCADE");
        }
        return clauses;
    }
}
