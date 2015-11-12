package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.RedefineSequenceAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueLogicPostgresql extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    public ActionResult execute(AddDefaultValueAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        Object defaultValue = action.defaultValue;

        DelegateResult result = (DelegateResult) super.execute(action, scope);

        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        if (defaultValue instanceof SequenceNextValueFunction) {
            result = new DelegateResult(result, new RedefineSequenceAction(
                    new ObjectReference(action.columnName.container.container, ((SequenceNextValueFunction) defaultValue).getValue()),
                    new StringClauses()
                            .append("OWNED BY")
                            .append(database.escapeObjectName(action.columnName.container)
                                    + "."
                                    + database.escapeObjectName(action.columnName))));
        }

        return result;
    }
}
