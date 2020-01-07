package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.logging.LogFactory;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MissingUniqueConstraintChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
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
        return new Class[]{Index.class};
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        List<Change> returnList = new ArrayList<Change>();

        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddUniqueConstraintChange change = createAddUniqueConstraintChange();
        change.setTableName(uc.getTable().getName());
        if (uc.getBackingIndex() != null && control.getIncludeTablespace()) {
            change.setTablespace(uc.getBackingIndex().getTablespace());
        }
        if (control.getIncludeCatalog()) {
            change.setCatalogName(uc.getTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(uc.getTable().getSchema().getName());
        }
        change.setConstraintName(uc.getName());
        change.setColumnNames(uc.getColumnNames());
        change.setDeferrable(uc.isDeferrable() ? Boolean.TRUE : null);
        change.setValidate(!uc.shouldValidate() ? Boolean.FALSE : null);
        change.setInitiallyDeferred(uc.isInitiallyDeferred() ? Boolean.TRUE : null);
        change.setDisabled(uc.isDisabled() ? Boolean.TRUE : null);
        if (referenceDatabase instanceof MSSQLDatabase) {
            change.setClustered(uc.isClustered() ? Boolean.TRUE : null);
        }

        if (comparisonDatabase instanceof OracleDatabase) {
            Index backingIndex = uc.getBackingIndex();
            if (backingIndex != null && backingIndex.getName() != null) {
                if (referenceDatabase.equals(comparisonDatabase) || !alreadyExists(backingIndex, comparisonDatabase, control)) {
                    Change[] changes = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
                    if (changes != null) {
                        returnList.addAll(Arrays.asList(changes));

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
            }
        }


        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }

        returnList.add(change);

        return returnList.toArray(new Change[returnList.size()]);


    }

    private boolean alreadyExists(Index backingIndex, Database comparisonDatabase, DiffOutputControl control) {
        boolean found = false;
        try {
            String catalogName = null;
            String schemaName = null;
            if (control.getIncludeCatalog()) {
                catalogName = backingIndex.getTable().getSchema().getCatalogName();
            }
            if (control.getIncludeSchema()) {
                schemaName = backingIndex.getTable().getSchema().getName();
            }

            Index backingIndexCopy = new Index(backingIndex.getName(), catalogName, schemaName, backingIndex.getTable().getName());
            for (Column column : backingIndex.getColumns()) {
                backingIndexCopy.addColumn(column);
            }

            // get the diffResult from the database object
            // This was set from DiffToChangeLog#generateChangeSets() so that we can access it here
            DiffResult diffResult = null;
            if (comparisonDatabase instanceof AbstractJdbcDatabase) {
                diffResult = (DiffResult) ((AbstractJdbcDatabase) comparisonDatabase).get("diffResult");
            }

            if (diffResult != null) {
                // check against the snapshot (better performance)
                Index foundIndex = diffResult.getComparisonSnapshot().get(backingIndexCopy);
                found = foundIndex != null;
            } else {
                // go to the db to find out
                found = SnapshotGeneratorFactory.getInstance().has(backingIndexCopy, comparisonDatabase);
            }
        } catch (Exception e) {
            LogFactory.getInstance().getLog().warning("Error checking for backing index "+backingIndex.toString()+": "+e.getMessage(), e);
        }
        return found;
    }

    protected AddUniqueConstraintChange createAddUniqueConstraintChange() {
        return new AddUniqueConstraintChange();
    }
}
