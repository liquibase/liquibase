package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Sequence;
import liquibase.structure.core.Table;

public class UnexpectedPrimaryKeyChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {
                Table.class,
        };
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
//        if (!diffResult.getObjectDiff(Table.class).getUnexpected().contains(pk.getTable())) {
        PrimaryKey pk = (PrimaryKey) unexpectedObject;
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalog().getName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());

        return new Change[] { change };
//        }

    }
}
