package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class MissingPrimaryKeyChangeGenerator implements MissingObjectChangeGenerator {

    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;

    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
            Table.class,
            Column.class,
            Index.class,
            UniqueConstraint.class
        };

    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        PrimaryKey pk = (PrimaryKey) missingObject;

        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());
        change.setColumnNames(pk.getColumnNames());
        if (control.isIncludeTablespace()) {
            change.setTablespace(pk.getTablespace());
        }

        control.setAlreadyHandledMissing(pk.getBackingIndex());

        return new Change[]{change};

    }
}
