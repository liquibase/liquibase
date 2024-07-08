package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateSequenceChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

public class MissingSequenceChangeGeneratorSnowflake extends MissingSequenceChangeGenerator{

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if ((Sequence.class.isAssignableFrom(objectType)) && (database instanceof SnowflakeDatabase)) {
            priority += PRIORITY_DATABASE;
        }
        return priority;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) missingObject;

        CreateSequenceChange change = new CreateSequenceChange();
        change.setSequenceName(sequence.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }
        change.setStartValue(sequence.getStartValue());
        change.setIncrementBy(sequence.getIncrementBy());

        return new Change[] { change };

    }
}
