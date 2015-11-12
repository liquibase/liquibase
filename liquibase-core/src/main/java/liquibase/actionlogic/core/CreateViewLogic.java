package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateViewLogic extends AbstractSqlBuilderLogic<CreateViewAction> {

    public static enum Clauses {
        createStatement, selectQuery,
    }

    @Override
    protected Class<CreateViewAction> getSupportedAction() {
        return CreateViewAction.class;
    }

    @Override
    public ValidationErrors validate(CreateViewAction action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkForRequiredField("viewName", action);
        validationErrors.checkForRequiredField("selectQuery", action);
        validationErrors.checkForDisallowedField("replaceIfExists", action, scope.getDatabase().getShortName());

        return validationErrors;
    }

    @Override
    public ActionResult execute(CreateViewAction action, Scope scope) throws ActionPerformException {
        if (ObjectUtil.defaultIfEmpty(action.fullDefinition, false)) {
            return new DelegateResult(new ExecuteSqlAction(action.selectQuery));
        } else {
            return new DelegateResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
        }
    }

    @Override
    protected StringClauses generateSql(CreateViewAction action, Scope scope) {
        Database database = scope.getDatabase();

        StringClauses clauses = new StringClauses();
        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            clauses.append(Clauses.createStatement, "CREATE OR REPLACE VIEW");
        } else {
            clauses.append(Clauses.createStatement, "CREATE VIEW");
        }

        clauses.append(database.escapeObjectName(action.viewName));
        clauses.append("AS");
        clauses.append(Clauses.selectQuery, action.selectQuery);

        return clauses;
    }
}
