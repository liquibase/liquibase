package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropColumnChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class UnexpectedColumnChangeGenerator implements UnexpectedObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Column.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                PrimaryKey.class,
                ForeignKey.class,
                Table.class,
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Column column = (Column) unexpectedObject;
//        if (!shouldModifyColumn(column)) {
//            continue;
//        }
//        if (column.relation instanceof View) {
//            return null;
//        }
//
//        if (column.relation.getSnapshotId() == null) { //not an actual table, maybe an alias, maybe in a different schema. Don't fix it.
//            return null;
//        }

        DropColumnChange change = new DropColumnChange();
//        change.setTableName(column.name.container.name);
//        if (control.getIncludeCatalog()) {
//            change.setCatalogName(column.name.container.container.container.name);
//        }
//        if (control.getIncludeSchema()) {
//            change.setSchemaName(column.name.container.container.name);
//        }
//        change.setColumnName(column.getSimpleName());

        return new Change[] { change };

    }
}
