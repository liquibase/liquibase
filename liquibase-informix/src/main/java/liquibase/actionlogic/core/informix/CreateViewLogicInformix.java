package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.View;

public class CreateViewLogicInformix extends CreateViewLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        ActionResult result = super.execute(action, scope);
        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            Database database = scope.get(Scope.Attr.database, Database.class);
            ObjectName viewName = action.get(CreateViewAction.Attr.viewName, ObjectName.class);

            return new DelegateResult(
                    new ExecuteSqlAction("DROP VIEW IF EXISTS "+database.escapeObjectName(viewName, View.class)),
                    ((DelegateResult) result).getActions().get(0));
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "CREATE VIEW");
        }

        return clauses;
    }
}
