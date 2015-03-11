package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AddFulltextConstraintChange;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;
import liquibase.structure.core.FulltextConstraint;
import liquibase.util.StringUtils;

public class MissingFulltextConstraintChangeGenerator implements MissingObjectChangeGenerator {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (FulltextConstraint.class.isAssignableFrom(objectType)) {
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
        
        if( !( referenceDatabase instanceof MySQLDatabase ) ){
            return null;
        }
        
        FulltextConstraint uc = (FulltextConstraint) missingObject;

        if (uc.getTable() == null) {
            return null;
        }

        AddFulltextConstraintChange change = new AddFulltextConstraintChange();
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
        
        String columnName = uc.getColumnNames().replace(",", "_").replace(" ", "");
        String indexName = StringUtils.oracleName( uc.getTable().getName(), columnName.trim() )+"_FDX";
        
        change.setConstraintName(indexName);
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
