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
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] {Column.class, PrimaryKey.class};
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Table unexpectedTable = (Table) unexpectedObject;

        DropTableChange change = new DropTableChange();
        change.setTableName(unexpectedTable.getSimpleName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(unexpectedTable.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(unexpectedTable.getSchema().getSimpleName());
        }

//        for (Column column : unexpectedTable.getColumns()) {
//            control.setAlreadyHandledUnexpected(column);
//        };
//        control.setAlreadyHandledUnexpected(unexpectedTable.primaryKey);

//        for (Index index : unexpectedTable.indexes) {
//            control.setAlreadyHandledUnexpected(index);
//        }
//        control.setAlreadyHandledUnexpected(unexpectedTable.primaryKey);
//        if (unexpectedTable.primaryKey != null) {
//            control.setAlreadyHandledUnexpected(unexpectedTable.primaryKey.getBackingIndex());
//        }

        return new Change[] { change };

    }
}
