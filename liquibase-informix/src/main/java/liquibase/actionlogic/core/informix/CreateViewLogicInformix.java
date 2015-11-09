package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.View;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateViewLogicInformix extends CreateViewLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public ActionResult execute(CreateViewAction action, Scope scope) throws ActionPerformException {
        ActionResult result = super.execute(action, scope);
        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            Database database = scope.getDatabase();
            ObjectReference viewName = action.viewName;

            return new DelegateResult(
                    new ExecuteSqlAction("DROP VIEW IF EXISTS "+database.escapeObjectName(viewName, View.class)),
                    ((DelegateResult) result).getActions().get(0));
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(CreateViewAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "CREATE VIEW");
        }

        return clauses;
    }
}
