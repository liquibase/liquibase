package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;

public class UnexpectedPrimaryKeyChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
       }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                Index.class
        };
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
//        if (!diffResult.getObjectDiff(Table.class).getUnexpected().contains(pk.getTable())) {
        PrimaryKey pk = (PrimaryKey) unexpectedObject;
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());

        Index backingIndex = pk.getBackingIndex();
        control.setAlreadyHandledUnexpected(backingIndex);


        return new Change[] { change };
//        }

    }
}
