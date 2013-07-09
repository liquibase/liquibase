package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Sequence;

public class MissingSequenceChangeGenerator implements MissingObjectChangeGenerator {

    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) missingObject;

        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName(sequence.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }
        change.setStartValue(sequence.getStartValue());
        change.setIncrementBy(sequence.getIncrementBy());

        return new Change[] { change };

    }
}
