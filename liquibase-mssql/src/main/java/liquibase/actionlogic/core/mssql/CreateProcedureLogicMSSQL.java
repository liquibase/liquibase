package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateProcedureAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.RewriteResult;
import liquibase.actionlogic.core.CreateProcedureLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.UnparsedSql;
import liquibase.structure.core.StoredProcedure;

import java.util.ArrayList;
import java.util.List;

public class CreateProcedureLogicMSSQL extends CreateProcedureLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.removeUnsupportedField(CreateProcedureAction.Attr.replaceIfExists);

        if (action.get(CreateProcedureAction.Attr.replaceIfExists, false) && !action.has(CreateProcedureAction.Attr.procedureName)) {
            errors.addError("procedureName is required if replaceIfExists is set to true");
        }
        return errors;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String procedureText = action.get(CreateProcedureAction.Attr.procedureText, String.class);
        String procedureName = action.get(CreateProcedureAction.Attr.procedureName, String.class);

        List<Action> actions = new ArrayList<>();
        if (action.get(CreateProcedureAction.Attr.replaceIfExists, false)) {
            actions.add(new ExecuteSqlAction("IF object_id('dbo."
                    +procedureName
                    +"', 'p') IS NULL EXEC ('CREATE PROCEDURE "
                    +database.escapeObjectName(procedureName, StoredProcedure.class)
                    +" AS SELECT 1 A')"));

            procedureText = procedureText.replaceFirst("(?i)create\\s+procedure", "ALTER PROCEDURE");
        }
        actions.add(new ExecuteSqlAction(procedureText));

        return new RewriteResult(actions.toArray(new Action[actions.size()]));

    }
}
