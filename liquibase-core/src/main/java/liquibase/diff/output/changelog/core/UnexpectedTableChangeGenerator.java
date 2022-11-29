package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropTableChange;
import liquibase.database.Database;
import liquibase.database.core.Db2zDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

public class UnexpectedTableChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {
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
        change.setTableName(unexpectedTable.getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(unexpectedTable.getSchema().getCatalogName());
        }
        // Because of different schema meaning in DB2 Z for OS we need to always include it to be able to drop
        if (control.getIncludeSchema() || referenceDatabase instanceof Db2zDatabase) {
            change.setSchemaName(unexpectedTable.getSchema().getName());
        }

        for (Column column : unexpectedTable.getColumns()) {
            control.setAlreadyHandledUnexpected(column);
        }
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
