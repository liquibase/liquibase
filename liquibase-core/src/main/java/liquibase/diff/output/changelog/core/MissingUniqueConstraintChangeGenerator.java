package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
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
        List<Change> returnList = new ArrayList<>();

        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddUniqueConstraintChange change = createAddUniqueConstraintChange();
        change.setTableName(uc.getTable().getName());
        if ((uc.getBackingIndex() != null) && control.getIncludeTablespace()) {
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
            if ((backingIndex != null) && (backingIndex.getName() != null)) {
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

    protected AddUniqueConstraintChange createAddUniqueConstraintChange() {
        return new AddUniqueConstraintChange();
    }
}
