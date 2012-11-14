package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropSequenceChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.Sequence;

public class UnexpectedSequenceChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        Sequence sequence = (Sequence) unexpectedObject;

        DropSequenceChange change = new DropSequenceChange();
        change.setSequenceName(sequence.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(sequence.getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(sequence.getSchema().getName());
        }

        return new Change[] { change };

    }
}
