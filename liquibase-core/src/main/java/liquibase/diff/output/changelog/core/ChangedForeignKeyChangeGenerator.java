package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Index;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtil;

public class ChangedForeignKeyChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
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

        StringUtil.StringUtilFormatter formatter = new StringUtil.StringUtilFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.toString(false);
            }
        };

        DropForeignKeyConstraintChange dropFkChange = new DropForeignKeyConstraintChange();
        dropFkChange.setConstraintName(fk.getName());
        dropFkChange.setBaseTableName(fk.getForeignKeyTable().getName());

        AddForeignKeyConstraintChange addFkChange = new AddForeignKeyConstraintChange();
        addFkChange.setConstraintName(fk.getName());
        addFkChange.setBaseTableName(fk.getForeignKeyTable().getName());
        addFkChange.setBaseColumnNames(StringUtil.join(fk.getForeignKeyColumns(), ",", formatter));
        addFkChange.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        addFkChange.setReferencedColumnNames(StringUtil.join(fk.getPrimaryKeyColumns(), ",", formatter));

        if (control.getIncludeCatalog()) {
            dropFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());

            addFkChange.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
            addFkChange.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());

            addFkChange.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
            addFkChange.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
        }

        if (fk.getBackingIndex() != null) {
            control.setAlreadyHandledChanged(fk.getBackingIndex());
        }

        return new Change[] { dropFkChange, addFkChange };
    }
}
