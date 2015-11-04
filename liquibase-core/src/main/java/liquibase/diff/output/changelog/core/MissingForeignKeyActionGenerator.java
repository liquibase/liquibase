package liquibase.diff.output.changelog.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectActionGenerator;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.snapshot.Snapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MissingForeignKeyActionGenerator implements MissingObjectActionGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[] {
                Table.class,
                Column.class,
                PrimaryKey.class,
                UniqueConstraint.class,
                Index.class
        };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public List<? extends Action> fixMissing(DatabaseObject missingObject, DiffOutputControl control, Snapshot referenceSnapshot, Snapshot targetSnapshot, Scope scope) {
        ForeignKey fk = (ForeignKey) missingObject;

        ArrayList<AddForeignKeysAction> actions = new ArrayList<>();
        actions.add(new AddForeignKeysAction(fk));

        return actions;

//        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
//        change.setConstraintName(fk.getSimpleName());
//
//        String defaultSchemaName = referenceSnapshot.getScope().getDatabase().getDefaultSchemaName();
//        String defaultCatalogName = referenceSnapshot.getScope().getDatabase().getDefaultCatalogName();
//
//        boolean includedCatalog = false;
//        change.setReferencedTableName(fk.getPrimaryKeyTable().getSimpleName());
//        if (referenceDatabase.supportsCatalogs() && (control.getIncludeCatalog() || (defaultCatalogName != null && !defaultCatalogName.equalsIgnoreCase(((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().getCatalogName())))) {
//            change.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
//            includedCatalog = true;
//        }
//
//        if (referenceDatabase.supportsSchemas() && (includedCatalog || control.getIncludeSchema() || (defaultSchemaName != null && !defaultSchemaName.equalsIgnoreCase(((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().getSimpleName())))) {
//            change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getSimpleName());
//        }
//        change.setReferencedColumnNames(StringUtils.join(fk.getPrimaryKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
//            @Override
//            public String toString(Column obj) {
//                return obj.getSimpleName();
//            }
//        }));
//
//        change.setBaseTableName(fk.getForeignKeyTable().getSimpleName());
//        if (control.getIncludeCatalog()) {
//            change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
//        }
//        if (control.getIncludeSchema()) {
//            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getSimpleName());
//        }
//        change.setBaseColumnNames(StringUtils.join(fk.getForeignKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
//            @Override
//            public String toString(Column obj) {
//                return obj.getSimpleName();
//            }
//        }));
//
//        change.setDeferrable(fk.isDeferrable());
//        change.setInitiallyDeferred(fk.isInitiallyDeferred());
//        change.setOnUpdate(fk.getUpdateRule());
//        change.setOnDelete(fk.getDeleteRule());
//
//        Index backingIndex = fk.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(fk.getForeignKeyTable());
//            for (String col : fk.getForeignKeyColumns().split("\\s*,\\s*")) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
//            control.setAlreadyHandledMissing(backingIndex);
//        }

//        return new Change[] { change };
    }
}
