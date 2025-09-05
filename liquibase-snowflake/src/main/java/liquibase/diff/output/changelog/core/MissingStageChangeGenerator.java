package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateStageChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Stage;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

/**
 * Generates CREATE STAGE changes for missing Stage objects during diff operations.
 */
public class MissingStageChangeGenerator extends AbstractChangeGenerator implements MissingObjectChangeGenerator {

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
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control,
                              Database referenceDatabase, Database comparisonDatabase,
                              ChangeGeneratorChain chain) {
        
        if (!(missingObject instanceof Stage)) {
            return null;
        }
        
        Stage stage = (Stage) missingObject;
        
        CreateStageChange change = new CreateStageChange();
        change.setStageName(stage.getName());
        
        // Add IF NOT EXISTS for safer changelog deployment
        change.setIfNotExists(true);
        
        // NOTE: Do not set explicit schema names in changelog generation
        // The target schema should be determined at deployment time via database.setDefaultSchemaName()
        // Setting explicit schema names would create objects in the source schema instead of target
        
        // Only set catalog name if it's different from default
        if (stage.getSchema() != null && stage.getSchema().getCatalog() != null) {
            // Only include catalog if it's not the default database
            String catalogName = stage.getSchema().getCatalog().getName();
            // TODO: Could add logic here to omit catalog if it matches target database
            change.setCatalogName(catalogName);
        }
        
        // Set all available properties
        if (stage.getUrl() != null) {
            change.setUrl(stage.getUrl());
        }
        if (stage.getStorageIntegration() != null) {
            change.setStorageIntegration(stage.getStorageIntegration());
        }
        if (stage.getComment() != null) {
            change.setComment(stage.getComment());
        }
        
        return new Change[]{change};
    }
}