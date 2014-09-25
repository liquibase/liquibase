package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.AbstractSnapshotLookupLogic;
import liquibase.snapshot.NewDatabaseSnapshot;
import liquibase.snapshot.SnapshotRelateLogic;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.Collection;

public class TableSnapshotGenerator extends AbstractSnapshotLookupLogic<Table> implements SnapshotRelateLogic {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        if (objectType.isAssignableFrom(Table.class)) {
            return PRIORITY_DEFAULT;
        } else {
            return PRIORITY_NONE;
        }

    }

    @Override
    public boolean supports(ExecutionEnvironment environment) {
        return true;
    }

    @Override
    public <T extends DatabaseObject> Collection<T> lookup(Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException {
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
    public void relate(NewDatabaseSnapshot snapshot) {
        for (Table table : snapshot.get(Table.class)) {
            Schema realSchema = snapshot.get(table.getSchema());
            if (realSchema != null) {
                realSchema.addDatabaseObject(table);
                table.setSchema(realSchema);
            }
        }
    }
}
