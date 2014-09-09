package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.snapshot.AbstractSnapshotLookupLogic;
import liquibase.snapshot.NewDatabaseSnapshot;
import liquibase.snapshot.SnapshotRelateLogic;
import liquibase.statement.core.SelectMetaDataStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.Collection;

public class ColumnLookup extends AbstractSnapshotLookupLogic<Column> implements SnapshotRelateLogic {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        if (objectType.isAssignableFrom(Column.class)) {
            return PRIORITY_OBJECT;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public boolean supports(ExecutionEnvironment environment) {
        return true;
    }

    @Override
    public <T extends DatabaseObject> Collection<T> lookup(Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) {
        try {
            Executor executor = ExecutorService.getInstance().getExecutor(environment.getTargetDatabase());
            if (example instanceof Schema) {
                return executor.query(new SelectMetaDataStatement(new Column().setRelation(new Table().setSchema((Schema) example))), environment).toList(objectType);
            } else if (example instanceof Relation) {
                return executor.query(new SelectMetaDataStatement(new Column().setRelation((Relation) example)), environment).toList(objectType);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public void relate(NewDatabaseSnapshot snapshot) {
        for (Column column : snapshot.get(Column.class)) {
            Relation exampleRelation = column.getRelation();

            Relation realRelation = snapshot.get(exampleRelation);
            realRelation.getColumns().add(column);
            column.setRelation(realRelation);
        }
    }
}
