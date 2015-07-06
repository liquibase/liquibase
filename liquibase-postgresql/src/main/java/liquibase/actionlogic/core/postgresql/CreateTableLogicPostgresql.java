package liquibase.actionlogic.core.postgresql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.*;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.postgresql.PostgresDatabase;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.util.StringClauses;

import java.util.List;

public class CreateTableLogicPostgresql extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return PostgresDatabase.class;
    }

    @Override
    protected StringClauses generateColumnSql(Column column, CreateTableAction action, Scope scope, List<Action> additionalActions) {
        Column.AutoIncrementInformation autoIncrementInformation = column.autoIncrementInformation;

        if (autoIncrementInformation != null) {
            String sequenceName = action.tableName.name + "_" + column.getSimpleName() + "_seq";

            AlterSequenceAction alterSequenceAction = (AlterSequenceAction) new AlterSequenceAction();
            alterSequenceAction.sequenceName = new ObjectName(action.tableName.container, sequenceName);

            alterSequenceAction.minValue = autoIncrementInformation.startWith;
            alterSequenceAction.incrementBy = autoIncrementInformation.incrementBy;
            additionalActions.add(alterSequenceAction
            );
        }


        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);
        return clauses;
    }
}
