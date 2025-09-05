package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterStageChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Stage;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;

/**
 * Generates ALTER STAGE changes for changed Stage objects during diff operations.
 */
public class ChangedStageChangeGenerator extends AbstractChangeGenerator implements ChangedObjectChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Stage.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DEFAULT;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return new Class[]{};
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[]{};
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences,
                              DiffOutputControl control, Database referenceDatabase, 
                              Database comparisonDatabase, ChangeGeneratorChain chain) {
        
        if (!(changedObject instanceof Stage)) {
            return null;
        }
        
        Stage stage = (Stage) changedObject;
        
        AlterStageChange change = new AlterStageChange();
        change.setStageName(stage.getName());
        
        if (stage.getSchema() != null) {
            if (stage.getSchema().getCatalog() != null) {
                change.setCatalogName(stage.getSchema().getCatalog().getName());
            }
            change.setSchemaName(stage.getSchema().getName());
        }
        
        // Apply differences to ALTER change (using correct API)
        if (differences.isDifferent("comment")) {
            change.setComment(stage.getComment());
        }
        
        if (differences.isDifferent("url")) {
            change.setUrl(stage.getUrl());
        }
        
        if (differences.isDifferent("storageIntegration")) {
            change.setStorageIntegration(stage.getStorageIntegration());
        }
        
        // Note: Many Stage properties are read-only or creation-time only
        // Focus on properties that can actually be altered in Snowflake
        
        return new Change[]{change};
    }
}