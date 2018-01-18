package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class UnexpectedUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
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
                Table.class,
                Index.class
        };
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) unexpectedObject;
        if (uc.getTable() == null) {
            return null;
        }

        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(uc.getTable().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(uc.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(uc.getTable().getSchema().getName());
        }
        change.setConstraintName(uc.getName());

        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledUnexpected(exampleIndex);
//        } else {
            control.setAlreadyHandledUnexpected(backingIndex);
//        }

        return new Change[] { change };
    }
}
