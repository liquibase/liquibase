package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.NewDatabaseSnapshot;
import liquibase.statement.Statement;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.diff.compare.DatabaseObjectComparatorFactory;
import liquibase.snapshot.AbstractSnapshotGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.DatabaseObjectCollection;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.Collection;

public class TableSnapshotGenerator extends AbstractSnapshotGenerator<Table> {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        if (objectType.isAssignableFrom(Table.class)) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NONE;
        }

    }

    @Override
    public <T extends DatabaseObject> Collection<T> lookupFor(DatabaseObject example, Class<T> objectType, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException {
        SelectMetaDataStatement statement;
        if (example instanceof Schema) {
            statement =  new SelectMetaDataStatement(new Table().setSchema(((Schema) example)));
        } else if (example instanceof Table) {
            statement = new SelectMetaDataStatement(example);
        } else {
            return null;
        }

        return ExecutorService.getInstance().getExecutor(environment.getTargetDatabase()).query(statement).toList(objectType);
    }

    @Override
    public void relate(Class<? extends DatabaseObject> objectType, NewDatabaseSnapshot snapshot) {
        if (Table.class.isAssignableFrom(objectType)) {
            for (Table table : snapshot.get(Table.class)) {
                Schema realSchema = snapshot.get(table.getSchema());
                realSchema.addDatabaseObject(table);
                table.setSchema(realSchema);
            }
        }
    }
}
