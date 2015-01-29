package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;

public class CreateViewLogic extends AbstractSqlBuilderLogic {

    public static enum Clauses {
        createStatement, selectQuery,
    }

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateViewAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors validationErrors = new ValidationErrors();

        validationErrors.checkForRequiredField(CreateViewAction.Attr.viewName, action);
        validationErrors.checkForRequiredField(CreateViewAction.Attr.selectQuery, action);
        validationErrors.checkForDisallowedField(CreateViewAction.Attr.replaceIfExists, action, scope.get(Scope.Attr.database, Database.class).getShortName());

        return validationErrors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        if (action.get(CreateViewAction.Attr.fullDefinition, false)) {
            return new RewriteResult(new ExecuteSqlAction(action.get(CreateViewAction.Attr.selectQuery, String.class)));
        } else {
            return new RewriteResult(new ExecuteSqlAction(generateSql(action, scope).toString()));
        }
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = new StringClauses();
        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            clauses.append(Clauses.createStatement, "CREATE OR REPLACE VIEW");
        } else {
            clauses.append(Clauses.createStatement, "CREATE VIEW");
        }

        clauses.append(database.escapeViewName(
                action.get(CreateViewAction.Attr.catalogName, String.class),
                action.get(CreateViewAction.Attr.schemaName, String.class),
                action.get(CreateViewAction.Attr.viewName, String.class)));
        clauses.append("AS");
        clauses.append(Clauses.selectQuery, action.get(CreateViewAction.Attr.selectQuery, String.class));

        return clauses;
    }
}
