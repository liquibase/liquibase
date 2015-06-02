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
    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        AutoIncrementDefinition autoIncrementDefinition = column.get(ColumnDefinition.Attr.autoIncrementDefinition, AutoIncrementDefinition.class);

        if (autoIncrementDefinition != null) {
            String sequenceName = action.get(CreateTableAction.Attr.tableName, String.class)+"_"+column.get(ColumnDefinition.Attr.columnName, String.class)+"_seq";

            additionalActions.add((AlterSequenceAction) new AlterSequenceAction()
                    .set(AlterSequenceAction.Attr.sequenceName, new ObjectName(action.get(CreateTableAction.Attr.tableName, ObjectName.class).getContainer(), sequenceName))
                    .set(AlterSequenceAction.Attr.minValue, autoIncrementDefinition.get(AutoIncrementDefinition.Attr.startWith, String.class))
                    .set(AlterSequenceAction.Attr.incrementBy, autoIncrementDefinition.get(AutoIncrementDefinition.Attr.incrementBy, String.class))
            );
        }


        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);
        return clauses;
    }
}
