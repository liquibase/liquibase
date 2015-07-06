package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

public class ChangedForeignKeyChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Index.class, UniqueConstraint.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) changedObject;

        StringUtils.StringUtilsFormatter formatter = new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        DropForeignKeyConstraintChange dropFkChange = new DropForeignKeyConstraintChange();
//        dropFkChange.setConstraintName(fk.getSimpleName());
//        dropFkChange.setBaseTableName(fk.getForeignKeyTable().getSimpleName());
//
        AddForeignKeyConstraintChange addFkChange = new AddForeignKeyConstraintChange();
//        addFkChange.setConstraintName(fk.getSimpleName());
//        addFkChange.setBaseTableName(fk.getForeignKeyTable().getSimpleName());
//        addFkChange.setBaseColumnNames(StringUtils.join(fk.getForeignKeyColumns(), ",", formatter));
//        addFkChange.setReferencedTableName(fk.getPrimaryKeyTable().getSimpleName());
//        addFkChange.setReferencedColumnNames(StringUtils.join(fk.getPrimaryKeyColumns(), ",", formatter));
//
//        if (control.getIncludeCatalog()) {
//            dropFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
//
//            addFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
//            addFkChange.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
//        }
//        if (control.getIncludeSchema()) {
//            dropFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getSimpleName());
//
//            addFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getSimpleName());
//            addFkChange.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getSimpleName());
//        }
//
//        if (fk.getBackingIndex() != null) {
//            control.setAlreadyHandledChanged(fk.getBackingIndex());
//        }

        return new Change[] { dropFkChange, addFkChange };
    }
}
