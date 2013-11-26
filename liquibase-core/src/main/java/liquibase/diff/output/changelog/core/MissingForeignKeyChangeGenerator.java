package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class MissingForeignKeyChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class,
                PrimaryKey.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] { Index.class, UniqueConstraint.class };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) missingObject;

        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName(fk.getName());

        change.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        if (control.isIncludeCatalog()) {
            change.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
        }
        change.setReferencedColumnNames(fk.getPrimaryKeyColumns());

        change.setBaseTableName(fk.getForeignKeyTable().getName());
        if (control.isIncludeCatalog()) {
            change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
        }
        change.setBaseColumnNames(fk.getForeignKeyColumns());

        change.setDeferrable(fk.isDeferrable());
        change.setInitiallyDeferred(fk.isInitiallyDeferred());
        change.setOnUpdate(fk.getUpdateRule());
        change.setOnDelete(fk.getDeleteRule());

        Index backingIndex = fk.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(fk.getForeignKeyTable());
//            for (String col : fk.getForeignKeyColumns().split("\\s*,\\s*")) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }

        return new Change[] { change };
    }
}
