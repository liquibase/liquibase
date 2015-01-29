package liquibase.actionlogic.core.mssql;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.CreateViewLogic;
import liquibase.database.Database;
import liquibase.database.core.firebird.FirebirdDatabase;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.UnparsedSql;

public class CreateViewLogicMSSQL extends CreateViewLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        ActionResult result = super.execute(action, scope);
        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            Database database = scope.get(Scope.Attr.database, Database.class);
            String catalogName = action.get(CreateViewAction.Attr.catalogName, String.class);
            String schemaName = action.get(CreateViewAction.Attr.schemaName, String.class);
            String viewName = action.get(CreateViewAction.Attr.viewName, String.class);

            //from http://stackoverflow.com/questions/163246/sql-server-equivalent-to-oracles-create-or-replace-view
            return new RewriteResult(new ExecuteSqlAction(
                    "IF NOT EXISTS (SELECT * FROM sys.views WHERE object_id = OBJECT_ID(N'["+ schemaName +"].["+viewName+"]')) "
                    + "EXEC sp_executesql N'CREATE VIEW "+database.escapeViewName(catalogName, schemaName, viewName)+"] AS SELECT ''This is a code stub which will be replaced by an Alter Statement'' as [code_stub]'"),
                    ((RewriteResult) result).getActions().get(0));
        }

        return result;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        if (action.get(CreateViewAction.Attr.replaceIfExists, false)) {
            clauses.replace(Clauses.createStatement, "ALTER VIEW");
        }

        return clauses;
    }
}
