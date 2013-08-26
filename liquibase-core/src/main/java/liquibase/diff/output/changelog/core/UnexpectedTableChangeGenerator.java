package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class UnexpectedTableChangeGenerator implements UnexpectedObjectChangeGenerator {
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                PrimaryKey.class,
                ForeignKey.class,
                Index.class,
                Column.class,
        };
    }

    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table unexpectedTable = (Table) unexpectedObject;

        DropTableChange change = new DropTableChange();
        change.setTableName(unexpectedTable.getName());
        if (control.isIncludeCatalog()) {
            change.setCatalogName(unexpectedTable.getSchema().getCatalogName());
        }
        if (control.isIncludeSchema()) {
            change.setSchemaName(unexpectedTable.getSchema().getName());
        }

        for (Column column : unexpectedTable.getColumns()) {
            control.setAlreadyHandledUnexpected(column);
        };
        control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey());

        for (Index index : unexpectedTable.getIndexes()) {
            control.setAlreadyHandledUnexpected(index);
        }
        control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey());
        if (unexpectedTable.getPrimaryKey() != null) {
            control.setAlreadyHandledUnexpected(unexpectedTable.getPrimaryKey().getBackingIndex());
        }

        return new Change[] { change };

    }
}
