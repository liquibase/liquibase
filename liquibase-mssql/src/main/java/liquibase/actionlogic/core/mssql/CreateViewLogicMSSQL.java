package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.View;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class CreateViewLogicMSSQL extends CreateViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(CreateViewAction action, Scope scope) throws ActionPerformException {
        ActionResult result = super.execute(action, scope);
        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            Database database = scope.getDatabase();
            ObjectReference viewName = action.viewName;

            //from http://stackoverflow.com/questions/163246/sql-server-equivalent-to-oracles-create-or-replace-view
            return new DelegateResult(new ExecuteSqlAction(
                    "IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+ viewName.container.name +"].["+viewName.name+"]')) "
                    + "EXEC sp_executesql N'CREATE VIEW "+database.escapeObjectName(viewName, View.class)+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"),
                    ((DelegateResult) result).getActions().get(0));
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(CreateViewAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "ALTER VIEW");
        }

        return clauses;
    }
}
