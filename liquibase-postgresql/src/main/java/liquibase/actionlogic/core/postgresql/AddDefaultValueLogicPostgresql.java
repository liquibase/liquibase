package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.RedefineSequenceAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.AddDefaultValueLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.statement.SequenceNextValueFunction;
import liquibase.structure.core.Column;

/**
 * Adds functionality for setting the sequence to be owned by the column with the default value
 */
public class AddDefaultValueLogicPostgresql extends AddDefaultValueLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        Object defaultValue = action.get(AddDefaultValueAction.Attr.defaultValue, Object.class);

        DelegateResult result = (DelegateResult) super.execute(action, scope);

        // for postgres, we need to also set the sequence to be owned by this table for true serial like functionality.
        // this will allow a drop table cascade to remove the sequence as well.
        if (defaultValue instanceof SequenceNextValueFunction) {
            result = new DelegateResult(result, new RedefineSequenceAction(
                    action.get(AddDefaultValueAction.Attr.catalogName, String.class),
                    action.get(AddDefaultValueAction.Attr.schemaName, String.class),
                    ((SequenceNextValueFunction) defaultValue).getValue(),
                    new StringClauses()
                            .append("OWNED BY")
                            .append(database.escapeTableName(
                                    action.get(AddDefaultValueAction.Attr.catalogName, String.class),
                                    action.get(AddDefaultValueAction.Attr.schemaName, String.class),
                                    action.get(AddDefaultValueAction.Attr.tableName, String.class))
                                    + "."
                                    + database.escapeObjectName(action.get(AddDefaultValueAction.Attr.columnName, String.class), Column.class))));
        }

        return result;
    }
}
