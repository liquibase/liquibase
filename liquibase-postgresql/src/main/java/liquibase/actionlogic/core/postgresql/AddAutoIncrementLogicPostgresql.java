package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddAutoIncrementAction;
import liquibase.action.core.AddDefaultValueAction;
import liquibase.action.core.CreateSequenceAction;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.statement.SequenceNextValueFunction;

public class AddAutoIncrementLogicPostgresql extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        String sequenceName = (action.get(AddAutoIncrementAction.Attr.tableName, String.class) + "_" + action.get(AddAutoIncrementAction.Attr.columnName, String.class) + "_seq").toLowerCase();

        String catalogName = action.get(AddAutoIncrementAction.Attr.catalogName, String.class);
        String schemaName = action.get(AddAutoIncrementAction.Attr.schemaName, String.class);
        String tableName = action.get(AddAutoIncrementAction.Attr.tableName, String.class);
        String columnName = action.get(AddAutoIncrementAction.Attr.columnName, String.class);
        String columnDataType = action.get(AddAutoIncrementAction.Attr.columnDataType, String.class);

        return new DelegateResult(
                new CreateSequenceAction(catalogName, schemaName, sequenceName),
                new SetNullableAction(catalogName, schemaName, tableName, columnName, null, false),
                new AddDefaultValueAction(catalogName, schemaName, tableName, columnName, columnDataType, new SequenceNextValueFunction((schemaName==null?"":schemaName+".")+sequenceName))
        );

    }
}
