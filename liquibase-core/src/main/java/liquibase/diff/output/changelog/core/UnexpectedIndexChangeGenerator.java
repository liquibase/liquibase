package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropIndexChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;

public class UnexpectedIndexChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                ForeignKey.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Index index = (Index) unexpectedObject;

//        if (index.getAssociatedWith().contains(Index.MARK_PRIMARY_KEY) || index.getAssociatedWith().contains(Index.MARK_FOREIGN_KEY) || index.getAssociatedWith().contains(Index.MARK_UNIQUE_CONSTRAINT)) {
//            return null;
//        }

        DropIndexChange change = new DropIndexChange();
        change.setTableName(index.getTable().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(index.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(index.getTable().getSchema().getName());
        }
        change.setIndexName(index.getName());
        change.setAssociatedWith(index.getAssociatedWithAsString());

        return new Change[] { change };

    }
}
