package liquibase.diff.output.changelog.core;

import liquibase.CatalogAndSchema;
import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.core.SchemaComparator;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.StringUtils;

public class MissingForeignKeyChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (ForeignKey.class.isAssignableFrom(objectType)) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
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
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        ForeignKey fk = (ForeignKey) missingObject;

        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setConstraintName(fk.getName());

        String defaultSchemaName = referenceDatabase.getDefaultSchemaName();
        String defaultCatalogName = referenceDatabase.getDefaultCatalogName();

        boolean includedCatalog = false;
        change.setReferencedTableName(fk.getPrimaryKeyTable().getName());

        String missingPrimaryKeyCatalogName = ((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().getCatalogName();
        if (referenceDatabase.supportsCatalogs()) {
            if (control.getIncludeCatalog()) {
                change.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
                includedCatalog = true;
            } else if (defaultCatalogName != null && !defaultCatalogName.equalsIgnoreCase(missingPrimaryKeyCatalogName)) {
                if (!(StringUtils.trimToEmpty(comparisonDatabase.getDefaultCatalogName()).equalsIgnoreCase(StringUtils.trimToEmpty(missingPrimaryKeyCatalogName)))) { //don't include catalogName if it's in the default catalog
                    change.setReferencedTableCatalogName(fk.getPrimaryKeyTable().getSchema().getCatalogName());
                    includedCatalog = true;
                }
            }
        }


        if (referenceDatabase.supportsSchemas()) {
            if (includedCatalog || control.getIncludeSchema()) {
                change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
            } else if ((defaultSchemaName != null && !defaultSchemaName.equalsIgnoreCase(((ForeignKey) missingObject).getPrimaryKeyTable().getSchema().getName()))) {
                if (!(StringUtils.trimToEmpty(comparisonDatabase.getDefaultSchemaName()).equalsIgnoreCase(StringUtils.trimToEmpty(fk.getPrimaryKeyTable().getSchema().getName())))) { //don't include schemaName if it's in the default schema
                    change.setReferencedTableSchemaName(fk.getPrimaryKeyTable().getSchema().getName());
                }

            }
        }

        change.setReferencedColumnNames(StringUtils.join(fk.getPrimaryKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.getName();
            }
        }));

        change.setBaseTableName(fk.getForeignKeyTable().getName());
        if (control.getIncludeCatalog()) {
            change.setBaseTableCatalogName(fk.getForeignKeyTable().getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setBaseTableSchemaName(fk.getForeignKeyTable().getSchema().getName());
        }
        change.setBaseColumnNames(StringUtils.join(fk.getForeignKeyColumns(), ",", new StringUtils.StringUtilsFormatter<Column>() {
            @Override
            public String toString(Column obj) {
                return obj.getName();
            }
        }));

        change.setDeferrable(fk.isDeferrable());
        change.setInitiallyDeferred(fk.isInitiallyDeferred());
        change.setValidate(fk.shouldValidate());
        change.setOnUpdate(fk.getUpdateRule());
        change.setOnDelete(fk.getDeleteRule());

        Index backingIndex = fk.getBackingIndex();
//        if (backingIndex == null) {
//            Index exampleIndex = new Index().setTable(fk.getForeignKeyTable());
//            for (String col : fk.getForeignKeyColumns().split("\\s*,\\s*")) {
//                exampleIndex.getColumns().add(col);
//            }
//            control.setAlreadyHandledMissing(exampleIndex);
//        } else {
            control.setAlreadyHandledMissing(backingIndex);
//        }

        return new Change[] { change };
    }
}
