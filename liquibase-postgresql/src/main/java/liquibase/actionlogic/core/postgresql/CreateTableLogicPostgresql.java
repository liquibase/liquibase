package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.*;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.structure.ObjectName;

import java.util.List;

public class CreateTableLogicPostgresql extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, CreateTableAction action, Scope scope, List<Action> additionalActions) {
        AutoIncrementDefinition autoIncrementDefinition = column.autoIncrementDefinition;

        if (autoIncrementDefinition != null) {
            String sequenceName = action.tableName.name + "_" + column.columnName.name + "_seq";

            AlterSequenceAction alterSequenceAction = (AlterSequenceAction) new AlterSequenceAction();
            alterSequenceAction.sequenceName = new ObjectName(action.tableName.container, sequenceName);

            alterSequenceAction.minValue = autoIncrementDefinition.startWith;
            alterSequenceAction.incrementBy = autoIncrementDefinition.incrementBy;
            additionalActions.add(alterSequenceAction
            );
        }


        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);
        return clauses;
    }
}
