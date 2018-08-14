package liquibase.diff.output.changelog.core;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.database.core.AbstractDb2Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.ArrayList;
import java.util.List;

public class MissingPrimaryKeyChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (PrimaryKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;

    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{
                Table.class,
                Column.class
        };

    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{
                Index.class
        };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<Change>();

        PrimaryKey pk = (PrimaryKey) missingObject;

        AddPrimaryKeyChange change = createAddPrimaryKeyChange();
        change.setTableName(pk.getTable().getName());
        if (control.getIncludeCatalog()) {
            change.setCatalogName(pk.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(pk.getTable().getSchema().getName());
        }
        change.setConstraintName(pk.getName());
        change.setColumnNames(pk.getColumnNames());
        if (control.getIncludeTablespace()) {
            change.setTablespace(pk.getTablespace());
        }

        if (referenceDatabase instanceof MSSQLDatabase && pk.getBackingIndex() != null && pk.getBackingIndex().getClustered() != null && !pk.getBackingIndex().getClustered()) {
            change.setClustered(false);
        }
        if (referenceDatabase instanceof PostgresDatabase && pk.getBackingIndex() != null && pk.getBackingIndex().getClustered() != null && pk.getBackingIndex().getClustered()) {
            change.setClustered(true);
        }

        returnList.add(change);

        return returnList.toArray(new Change[returnList.size()]);

    }

    protected AddPrimaryKeyChange createAddPrimaryKeyChange() {
        return new AddPrimaryKeyChange();
    }

}
