package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterSequenceChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

import java.util.ArrayList;
import java.util.List;

public class ChangedSequenceChangeGeneratorSnowflake extends ChangedSequenceChangeGenerator{

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if ((Sequence.class.isAssignableFrom(objectType)) && (database instanceof SnowflakeDatabase)) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) changedObject;

        List<Change> changes = new ArrayList<>();
        AlterSequenceChange accumulatedChange = createAlterSequenceChange(sequence, control);

        if (differences.isDifferent("incrementBy")) {
            AlterSequenceChange change = createAlterSequenceChange(sequence, control);
            change.setIncrementBy(sequence.getIncrementBy());
            accumulatedChange.setIncrementBy(sequence.getIncrementBy());
            changes.add(change);
        }

        if (changes.isEmpty()) {
            return null;
        } else {
            return changes.toArray(EMPTY_CHANGE);
        }
    }
}
