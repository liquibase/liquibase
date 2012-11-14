package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.UniqueConstraint;

public class UnexpectedUniqueConstraintChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Sequence.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) unexpectedObject;
        if (uc.getTable() == null) {
            return null;
        }

        DropUniqueConstraintChange change = new DropUniqueConstraintChange();
        change.setTableName(uc.getTable().getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(uc.getTable().getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(uc.getTable().getSchema().getName());
        }
        change.setConstraintName(uc.getName());

        return new Change[] { change };
    }
}
