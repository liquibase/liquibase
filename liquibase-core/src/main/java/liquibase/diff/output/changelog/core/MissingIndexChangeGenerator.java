package liquibase.diff.output.changelog.core;

import liquibase.change.AddColumnConfig;
import liquibase.change.Change;
import liquibase.change.core.CreateIndexChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class MissingIndexChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Index.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        Index index = (Index) missingObject;

        CreateIndexChange change = createCreateIndexChange();
        change.setTableName(index.getTable().getName());
        if (control.getIncludeTablespace()) {
            change.setTablespace(index.getTablespace());
        }
        if (control.getIncludeCatalog()) {
            change.setCatalogName(index.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(index.getTable().getSchema().getName());
        }
        change.setIndexName(index.getName());
        change.setUnique(index.isUnique());
        change.setAssociatedWith(index.getAssociatedWithAsString());
        change.setClustered(index.getClustered());

//        if (index.getAssociatedWith().contains(Index.MARK_PRIMARY_KEY) || index.getAssociatedWith().contains(Index.MARK_FOREIGN_KEY) || index.getAssociatedWith().contains(Index.MARK_UNIQUE_CONSTRAINT)) {
//            continue;
//        }

        for (Column column : index.getColumns()) {
            change.addColumn(new AddColumnConfig(column));
        }

        return new Change[] { change };
    }

    protected CreateIndexChange createCreateIndexChange() {
        return new CreateIndexChange();
    }
}
