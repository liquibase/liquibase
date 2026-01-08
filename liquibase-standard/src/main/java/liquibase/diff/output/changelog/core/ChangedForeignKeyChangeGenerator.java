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
import liquibase.structure.core.*;
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

        StringUtil.StringUtilFormatter<Column> formatter = obj -> obj.toString(false);

        // Use the comparison object for the drop statement to get the correct constraint name from the target database
        ForeignKey comparisonFk = (ForeignKey) differences.getComparisonObject();
        if (comparisonFk == null) {
            comparisonFk = fk; // Fallback to reference object if comparison not available
        }

        DropForeignKeyConstraintChange dropFkChange = new DropForeignKeyConstraintChange();
        dropFkChange.setConstraintName(comparisonFk.getName());
        dropFkChange.setBaseTableName(comparisonFk.getForeignKeyTable().getName());

        AddForeignKeyConstraintChange addFkChange = new AddForeignKeyConstraintChange();
        addFkChange.setConstraintName(fk.getName());
        addFkChange.setBaseTableName(fk.getForeignKeyTable().getName());
        addFkChange.setBaseColumnNames(StringUtil.join(fk.getForeignKeyColumns(), ",", formatter));
        addFkChange.setReferencedTableName(fk.getPrimaryKeyTable().getName());
        addFkChange.setReferencedColumnNames(StringUtil.join(fk.getPrimaryKeyColumns(), ",", formatter));
        addFkChange.setOnDelete(fk.getDeleteRule());
        addFkChange.setOnUpdate(fk.getUpdateRule());

        Schema comparissonSchema = comparisonFk.getForeignKeyTable().getSchema();
        Schema referenceSchema = fk.getPrimaryKeyTable().getSchema();
        Schema fkSchema = fk.getForeignKeyTable().getSchema();
        if (control.getIncludeCatalog() && comparissonSchema != null && referenceSchema != null && fkSchema != null) {
            dropFkChange.setBaseTableCatalogName(comparissonSchema.getCatalogName());

            addFkChange.setBaseTableCatalogName(fkSchema.getCatalogName());
            addFkChange.setReferencedTableCatalogName(referenceSchema.getCatalogName());
        }
        if (control.getIncludeSchema() && comparissonSchema != null && referenceSchema != null && fkSchema != null) {
            dropFkChange.setBaseTableSchemaName(comparissonSchema.getName());

            addFkChange.setBaseTableSchemaName(fkSchema.getName());
            addFkChange.setReferencedTableSchemaName(referenceSchema.getName());
        }

        if (fk.getBackingIndex() != null) {
            control.setAlreadyHandledChanged(fk.getBackingIndex());
        }

        return new Change[] { dropFkChange, addFkChange };
    }
}
