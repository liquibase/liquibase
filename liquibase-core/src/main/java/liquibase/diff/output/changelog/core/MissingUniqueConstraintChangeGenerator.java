package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class MissingUniqueConstraintChangeGenerator implements MissingObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(uc.getTable().getName());
        if (uc.getBackingIndex() != null && control.isIncludeTablespace()) {
            change.setTablespace(uc.getBackingIndex().getTablespace());
        }
        if (control.isIncludeCatalog()) {
            change.setCatalogName(uc.getTable().getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(uc.getTable().getSchema().getName());
        }
        change.setConstraintName(uc.getName());
        change.setColumnNames(uc.getColumnNames());
        change.setDeferrable(uc.isDeferrable());
        change.setInitiallyDeferred(uc.isInitiallyDeferred());
        change.setDisabled(uc.isDisabled());

        return new Change[]{change};


    }
}
