package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;

public class MissingPrimaryKeyChangeGenerator implements MissingObjectChangeGenerator {

    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;

    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        PrimaryKey pk = (PrimaryKey) missingObject;

        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());
        change.setColumnNames(pk.getColumnNames());
        if (control.isIncludeTablespace()) {
            change.setTablespace(pk.getTablespace());
        }

        return new Change[] { change };

    }
}
