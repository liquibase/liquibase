package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;

public class MissingUniqueConstraintChangeGenerator implements MissingObjectChangeGenerator {
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
        UniqueConstraint uc = (UniqueConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
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
        change.setDeferrable(uc.isDeferrable());
        change.setInitiallyDeferred(uc.isInitiallyDeferred());
        change.setDisabled(uc.isDisabled());

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


        return new Change[]{change};


    }
}
