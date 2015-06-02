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
import liquibase.structure.ObjectName;

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
        ObjectName columnName = action.get(AddAutoIncrementAction.Attr.columnName, ObjectName.class);
        ObjectName tableName = columnName.getContainer();

        ObjectName sequenceName = new ObjectName(tableName.getContainer(), (tableName.getName() + "_" + columnName.getName() + "_seq").toLowerCase());

        String columnDataType = action.get(AddAutoIncrementAction.Attr.columnDataType, String.class);

        return new DelegateResult(
                new CreateSequenceAction(sequenceName),
                new SetNullableAction(columnName, null, false),
                new AddDefaultValueAction(columnName, columnDataType, new SequenceNextValueFunction(sequenceName))
        );

    }
}
