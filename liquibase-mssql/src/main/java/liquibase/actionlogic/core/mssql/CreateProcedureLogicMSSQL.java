package liquibase.actionlogic.core.mssql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.CreateProcedureAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.CreateProcedureLogic;
import liquibase.database.Database;
import liquibase.database.core.mssql.MSSQLDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.StoredProcedure;
import liquibase.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateProcedureLogicMSSQL extends CreateProcedureLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MSSQLDatabase.class;
    }

    @Override
    public ValidationErrors validate(CreateProcedureAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.removeUnsupportedField("replaceIfExists");

        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false) && action.procedureName == null) {
            errors.addError("procedureName is required if replaceIfExists is set to true");
        }
        return errors;
    }

    @Override
    public ActionResult execute(CreateProcedureAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        String procedureText = action.procedureText.toString();
        ObjectReference procedureName = action.procedureName;

        List<Action> actions = new ArrayList<>();
        if (ObjectUtil.defaultIfEmpty(action.replaceIfExists, false)) {
            actions.add(new ExecuteSqlAction("IF object_id('dbo."
                    +procedureName
                    +"', 'p') IS NULL EXEC ('CREATE PROCEDURE "
                    +database.escapeObjectName(procedureName, StoredProcedure.class)
                    +" AS SELECT 1 A')"));

            procedureText = procedureText.replaceFirst("(?i)create\\s+procedure", "ALTER PROCEDURE");
        }
        actions.add(new ExecuteSqlAction(procedureText));

        return new DelegateResult(actions.toArray(new Action[actions.size()]));

    }
}
