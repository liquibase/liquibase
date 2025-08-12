package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropStageChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Stage;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.AbstractChangeGenerator;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

/**
 * Generates DROP STAGE changes for unexpected Stage objects during diff operations.
 */
public class UnexpectedStageChangeGenerator extends AbstractChangeGenerator implements UnexpectedObjectChangeGenerator {

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
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control,
                                 Database referenceDatabase, Database comparisonDatabase,
                                 ChangeGeneratorChain chain) {
        
        if (!(unexpectedObject instanceof Stage)) {
            return null;
        }
        
        Stage stage = (Stage) unexpectedObject;
        
        DropStageChange change = new DropStageChange();
        change.setStageName(stage.getName());
        
        if (stage.getSchema() != null) {
            if (stage.getSchema().getCatalog() != null) {
                change.setCatalogName(stage.getSchema().getCatalog().getName());
            }
            change.setSchemaName(stage.getSchema().getName());
        }
        
        // Set conditional drop flag for safety
        change.setIfExists(true);
        
        return new Change[]{change};
    }
}