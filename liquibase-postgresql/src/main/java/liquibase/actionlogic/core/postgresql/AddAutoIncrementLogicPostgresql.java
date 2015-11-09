package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
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
import liquibase.structure.ObjectReference;
import liquibase.structure.core.DataType;

public class AddAutoIncrementLogicPostgresql extends AbstractActionLogic<AddAutoIncrementAction> {

    @Override
    protected Class<AddAutoIncrementAction> getSupportedAction() {
        return AddAutoIncrementAction.class;
    }

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    public ActionResult execute(AddAutoIncrementAction action, Scope scope) throws ActionPerformException {
        ObjectReference columnName = action.columnName;
        ObjectReference tableName = columnName.container;

        ObjectReference sequenceName = new ObjectReference(tableName.container, (tableName.name + "_" + columnName.name + "_seq").toLowerCase());

        DataType columnDataType = action.columnDataType;

        return new DelegateResult(
                new CreateSequenceAction(sequenceName),
                new SetNullableAction(columnName, null, false),
                new AddDefaultValueAction(columnName, columnDataType, new SequenceNextValueFunction(sequenceName))
        );

    }
}
