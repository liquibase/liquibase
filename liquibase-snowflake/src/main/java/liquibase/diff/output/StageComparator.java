package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Stage;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;

import java.util.Objects;
import java.util.Set;

/**
 * Snowflake Stage diff comparator.
 * Compares Stage objects for differences during diff operations.
 * Focuses on configuration properties while excluding state properties.
 */
public class StageComparator implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Stage.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        Stage stage = (Stage) databaseObject;
        // Hash includes name, catalogName, schemaName for identity
        String catalogName = stage.getSchema() != null && stage.getSchema().getCatalog() != null ? 
                           stage.getSchema().getCatalog().getName() : "";
        String schemaName = stage.getSchema() != null && stage.getSchema().getName() != null ? 
                           stage.getSchema().getName() : "";
        return new String[] { stage.getName(), catalogName, schemaName };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                               Database accordingTo, DatabaseObjectComparatorChain chain) {
        
        if (!(databaseObject1 instanceof Stage) || !(databaseObject2 instanceof Stage)) {
            return false;
        }

        Stage stage1 = (Stage) databaseObject1;
        Stage stage2 = (Stage) databaseObject2;

        // Use the Stage's equals method for identity comparison
        return stage1.equals(stage2);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                           Database accordingTo, CompareControl compareControl,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        if (!(databaseObject1 instanceof Stage) || !(databaseObject2 instanceof Stage)) {
            return differences;
        }

        Stage stage1 = (Stage) databaseObject1;
        Stage stage2 = (Stage) databaseObject2;

        // CONFIGURATION PROPERTIES - Always compare
        
        // Core stage configuration (only properties that exist in Stage class)
        compareProperty("url", stage1.getUrl(), stage2.getUrl(), differences);
        compareProperty("stageType", stage1.getStageType(), stage2.getStageType(), differences);
        compareProperty("stageRegion", stage1.getStageRegion(), stage2.getStageRegion(), differences);
        compareProperty("storageIntegration", stage1.getStorageIntegration(), stage2.getStorageIntegration(), differences);
        compareProperty("comment", stage1.getComment(), stage2.getComment(), differences);
        
        // Operational properties (from SHOW STAGES)
        compareProperty("hasCredentials", stage1.getHasCredentials(), stage2.getHasCredentials(), differences);
        compareProperty("hasEncryptionKey", stage1.getHasEncryptionKey(), stage2.getHasEncryptionKey(), differences);
        compareProperty("cloud", stage1.getCloud(), stage2.getCloud(), differences);
        compareProperty("directoryEnabled", stage1.getDirectoryEnabled(), stage2.getDirectoryEnabled(), differences);
        
        // NOTE: State properties (owner, created, lastAltered) are excluded per pattern
        // These are read-only properties that don't affect stage configuration
        
        return differences;
    }

    /**
     * Helper method to compare individual properties and add differences if found.
     */
    private void compareProperty(String propertyName, Object value1, Object value2, ObjectDifferences differences) {
        if (!Objects.equals(value1, value2)) {
            differences.addDifference(propertyName, value1, value2);
        }
    }

    /**
     * Compare only when both properties are present (non-null) - for optional properties
     */
    private void compareWhenPresent(String propertyName, Object value1, Object value2, ObjectDifferences differences) {
        if (value1 != null && value2 != null) {
            compareProperty(propertyName, value1, value2, differences);
        }
    }
}