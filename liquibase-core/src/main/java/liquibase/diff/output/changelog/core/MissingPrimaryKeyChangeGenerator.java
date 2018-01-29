package liquibase.diff.output.changelog.core;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.core.AddPrimaryKeyChange;
import liquibase.database.Database;
import liquibase.database.core.*;
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
        List<Change> returnList = new ArrayList<>();

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

        if ((referenceDatabase instanceof MSSQLDatabase) && (pk.getBackingIndex() != null) && (pk.getBackingIndex()
            .getClustered() != null) && !pk.getBackingIndex().getClustered()) {
            change.setClustered(false);
        }
        if ((referenceDatabase instanceof PostgresDatabase) && (pk.getBackingIndex() != null) && (pk.getBackingIndex
            ().getClustered() != null) && pk.getBackingIndex().getClustered()) {
            change.setClustered(true);
        }

        if ((comparisonDatabase instanceof OracleDatabase) || ((comparisonDatabase instanceof AbstractDb2Database) && (pk
            .getBackingIndex() != null) && !comparisonDatabase.isSystemObject(pk.getBackingIndex()))) {
            Index backingIndex = pk.getBackingIndex();
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
                try {
                    if (!control.getIncludeCatalog() && !control.getIncludeSchema()) {
                        CatalogAndSchema schema = comparisonDatabase.getDefaultSchema().customize(comparisonDatabase);
                        backingIndex.getTable().setSchema(schema.getCatalogName(), schema.getSchemaName()); //set table schema so it is found in the correct schema
                    }
                    if (referenceDatabase.equals(comparisonDatabase) || !SnapshotGeneratorFactory.getInstance().has(backingIndex, comparisonDatabase)) {
                        Change[] fixes = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);

                        if (fixes != null) {
                            for (Change fix : fixes) {
                                if (fix != null) {
                                    returnList.add(fix);
                                }
                            }
                        }

                    }
                } catch (Exception e) {
                    throw new UnexpectedLiquibaseException(e);
                }


                change.setForIndexName(backingIndex.getName());
                Schema schema = backingIndex.getSchema();
                if (schema != null) {
                    if (control.getIncludeCatalog()) {
                        change.setForIndexCatalogName(schema.getCatalogName());
                    }
                    if (control.getIncludeSchema()) {
                        change.setForIndexSchemaName(schema.getName());
                    }
                }
            }
        }

        control.setAlreadyHandledMissing(pk.getBackingIndex());
        returnList.add(change);

        return returnList.toArray(new Change[returnList.size()]);

    }

    protected AddPrimaryKeyChange createAddPrimaryKeyChange() {
        return new AddPrimaryKeyChange();
    }

}
