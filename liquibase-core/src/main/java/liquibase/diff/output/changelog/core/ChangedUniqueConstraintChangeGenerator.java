package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.structure.core.UniqueConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChangedUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{Index.class};
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<>();

        UniqueConstraint uniqueConstraint = (UniqueConstraint) changedObject;

        DropUniqueConstraintChange dropUniqueConstraintChange = createDropUniqueConstraintChange();
        dropUniqueConstraintChange.setTableName(uniqueConstraint.getTable().getName());
        dropUniqueConstraintChange.setConstraintName(uniqueConstraint.getName());

        AddUniqueConstraintChange addUniqueConstraintChange = createAddUniqueConstraintChange();
        addUniqueConstraintChange.setConstraintName(uniqueConstraint.getName());
        addUniqueConstraintChange.setTableName(uniqueConstraint.getTable().getName());
        addUniqueConstraintChange.setColumnNames(uniqueConstraint.getColumnNames());

        returnList.add(dropUniqueConstraintChange);

        if (control.getIncludeCatalog()) {
            dropUniqueConstraintChange.setCatalogName(uniqueConstraint.getSchema().getCatalogName());
            addUniqueConstraintChange.setCatalogName(uniqueConstraint.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            dropUniqueConstraintChange.setSchemaName(uniqueConstraint.getSchema().getName());
            addUniqueConstraintChange.setSchemaName(uniqueConstraint.getSchema().getName());
        }

        Index backingIndex = uniqueConstraint.getBackingIndex();
        if (comparisonDatabase instanceof OracleDatabase) {
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                Change[] missingIndexChanges = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                if (missingIndexChanges != null) {
                    returnList.addAll(Arrays.asList(missingIndexChanges));
                }

                addUniqueConstraintChange.setForIndexName(backingIndex.getName());
                Schema schema = backingIndex.getSchema();
                if (schema != null) {
                    if (control.getIncludeCatalog()) {
                        addUniqueConstraintChange.setForIndexCatalogName(schema.getCatalogName());
                    }
                    if (control.getIncludeSchema()) {
                        addUniqueConstraintChange.setForIndexSchemaName(schema.getName());
                    }
                }
            }
        }

        control.setAlreadyHandledChanged(backingIndex);

        returnList.add(addUniqueConstraintChange);

        return returnList.toArray(new Change[returnList.size()]);
    }

    protected DropUniqueConstraintChange createDropUniqueConstraintChange() {
        return new DropUniqueConstraintChange();
    }

    protected AddUniqueConstraintChange createAddUniqueConstraintChange() {
        return new AddUniqueConstraintChange();
    }
}
