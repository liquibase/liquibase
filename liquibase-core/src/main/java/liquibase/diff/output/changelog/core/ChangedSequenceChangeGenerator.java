package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterSequenceChange;
import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class ChangedSequenceChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) changedObject;

        List<Change> changes = new ArrayList<Change>();
        AlterSequenceChange accumulatedChange = createAlterSequenceChange(sequence, control);

        if (differences.isDifferent("incrementBy")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setIncrementBy(sequence.getIncrementBy());
            accumulatedChange.setIncrementBy(sequence.getIncrementBy());
            changes.add(change);
        }

        if (differences.isDifferent("maxValue")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setMaxValue(sequence.getMaxValue());
            accumulatedChange.setMaxValue(sequence.getMaxValue());
            changes.add(change);
        }

        if (differences.isDifferent("minValue")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setMinValue(sequence.getMinValue());
            accumulatedChange.setMinValue(sequence.getMinValue());
            changes.add(change);
        }

        if (differences.isDifferent("ordered")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setOrdered(sequence.getOrdered());
            accumulatedChange.setOrdered(sequence.getOrdered());
            changes.add(change);
        }

        if (differences.isDifferent("cacheSize")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setCacheSize(sequence.getCacheSize());
            accumulatedChange.setCacheSize(sequence.getCacheSize());
            changes.add(change);
        }

        if (differences.isDifferent("willCycle")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setCycle(sequence.getWillCycle());
            accumulatedChange.setCycle(sequence.getWillCycle());
            changes.add(change);
        }

        if (differences.isDifferent("dataType")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setDataType(sequence.getDataType());
            accumulatedChange.setDataType(sequence.getDataType());
            changes.add(change);
        }

        if (changes.size() == 0) {
            return null;
        } else if (comparisonDatabase instanceof PostgresDatabase) {
            return new Change[] {accumulatedChange};
        } else {
            return changes.toArray(new Change[changes.size()]);
        }
    }

    protected AlterSequenceChange createAlterSequenceChange(Sequence sequence, DiffOutputControl control) {
        AlterSequenceChange change = new AlterSequenceChange();
        if (control.getIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }
        change.setSequenceName(sequence.getName());
        return change;
    }
}
