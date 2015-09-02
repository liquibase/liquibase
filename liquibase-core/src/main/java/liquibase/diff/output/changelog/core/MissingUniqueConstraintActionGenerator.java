package liquibase.diff.output.changelog.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddUniqueConstraintsAction;
import liquibase.change.Change;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.MissingObjectActionGenerator;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;

import java.util.ArrayList;
import java.util.List;

public class MissingUniqueConstraintActionGenerator implements MissingObjectActionGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        if (UniqueConstraint.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
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
    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        List<Action> returnList = new ArrayList<>();
        returnList.add(new AddUniqueConstraintsAction((UniqueConstraint) missingObject));

        return returnList;
//        change.setTableName(uc.getTable().getSimpleName());
//        if (uc.getBackingIndex() != null && control.getIncludeTablespace()) {
//            change.setTablespace(uc.getBackingIndex().getTablespace());
//        }
//        if (control.getIncludeCatalog()) {
//            change.setCatalogName(uc.getTable().getSchema().getCatalogName());
//        }
//        if (control.getIncludeSchema()) {
//            change.setSchemaName(uc.getTable().getSchema().getSimpleName());
//        }
//        change.setConstraintName(uc.getSimpleName());
//        change.setColumnNames(uc.getColumnNames());
//        change.setDeferrable(uc.isDeferrable() ? Boolean.TRUE : null);
//        change.setInitiallyDeferred(uc.isInitiallyDeferred() ? Boolean.TRUE : null);
//        change.setDisabled(uc.isDisabled() ? Boolean.TRUE : null);

//        if (comparisonDatabase instanceof OracleDatabase) {
//            Index backingIndex = uc.getBackingIndex();
//            if (backingIndex != null && backingIndex.getName() != null) {
//                Change[] changes = ChangeGeneratorFactory.getInstance().fixMissing(backingIndex, control, referenceDatabase, comparisonDatabase);
//                if (changes != null) {
//                    returnList.addAll(Arrays.asList(changes));
//
//                    change.setForIndexName(backingIndex.getName());
//                    Schema schema = backingIndex.getSchema();
//                    if (schema != null) {
//                        if (control.getIncludeCatalog()) {
//                            change.setForIndexCatalogName(schema.getCatalogName());
//                        }
//                        if (control.getIncludeSchema()) {
//                            change.setForIndexSchemaName(schema.getName());
//                        }
//                    }
//                }
//            }
//        }


//        Index backingIndex = uc.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(uc.getTable());
//            for (String col : uc.getColumns()) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
//            control.setAlreadyHandledMissing(backingIndex);
//        }

//        returnList.add(change);

    }
}
