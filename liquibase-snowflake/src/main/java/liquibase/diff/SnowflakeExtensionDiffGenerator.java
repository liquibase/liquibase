package liquibase.diff;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.database.object.Warehouse;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.output.FileFormatComparator;
import liquibase.diff.output.WarehouseComparator;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Unified diff generator for all Snowflake extension objects.
 * 
 * This replaces individual FileFormatDiffGenerator, etc. with a single
 * unified approach that handles both schema-level and account-level objects
 * consistently.
 * 
 * Supported object types:
 * - FileFormat (schema-level)
 * - Warehouse (account-level)  
 * - Future extension objects...
 */
public class SnowflakeExtensionDiffGenerator implements DiffGenerator {
    
    // Registry of supported extension object types
    private static final Set<Class<? extends DatabaseObject>> SUPPORTED_EXTENSION_OBJECTS = new HashSet<>(Arrays.asList(
        FileFormat.class,
        Warehouse.class
        // Add future extension objects here...
    ));
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;  // High priority for Snowflake extension objects
    }
    
    @Override
    public boolean supports(Database referenceDatabase, Database comparisonDatabase) {
        return referenceDatabase instanceof SnowflakeDatabase || comparisonDatabase instanceof SnowflakeDatabase;
    }
    
    @Override
    public DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, 
                            CompareControl compareControl) throws DatabaseException {
        
        System.out.println("🔧 SnowflakeExtensionDiffGenerator.compare() called");
        
        // Create base diff result
        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, compareControl);
        
        // Handle all supported extension object types
        for (Class<? extends DatabaseObject> objectType : SUPPORTED_EXTENSION_OBJECTS) {
            System.out.println("🔧 SnowflakeExtensionDiffGenerator: Comparing " + objectType.getSimpleName() + " objects");
            compareExtensionObjects(objectType, referenceSnapshot, comparisonSnapshot, compareControl, diffResult);
        }
        
        return diffResult;
    }
    
    /**
     * Generic extension object comparison that works for any extension object type.
     * This handles both schema-level (FileFormat) and account-level (Warehouse) objects.
     */
    @SuppressWarnings("unchecked")
    private <T extends DatabaseObject> void compareExtensionObjects(
            Class<T> objectType, 
            DatabaseSnapshot referenceSnapshot, 
            DatabaseSnapshot comparisonSnapshot,
            CompareControl compareControl, 
            DiffResult diffResult) {
        
        System.out.println("🔧 SnowflakeExtensionDiffGenerator: Comparing " + objectType.getSimpleName() + " objects directly...");
        
        Set<T> referenceObjects = referenceSnapshot.get(objectType);
        Set<T> comparisonObjects = comparisonSnapshot.get(objectType);
        
        System.out.println("🔧 SnowflakeExtensionDiffGenerator: Reference " + objectType.getSimpleName() + " objects: " + 
                         (referenceObjects != null ? referenceObjects.size() : "null"));
        System.out.println("🔧 SnowflakeExtensionDiffGenerator: Comparison " + objectType.getSimpleName() + " objects: " + 
                         (comparisonObjects != null ? comparisonObjects.size() : "null"));
        
        if (referenceObjects == null) referenceObjects = new HashSet<>();
        if (comparisonObjects == null) comparisonObjects = new HashSet<>();
        
        // Get appropriate comparator for this object type
        DatabaseObjectComparator comparator = getComparatorForType(objectType);
        Database database = comparisonSnapshot.getDatabase();
        
        // Find missing objects (in reference but not in comparison)
        for (T refObject : referenceObjects) {
            boolean found = false;
            for (T compObject : comparisonObjects) {
                if (comparator.isSameObject(refObject, compObject, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGenerator: Found missing " + objectType.getSimpleName() + ": " + getObjectName(refObject));
                diffResult.addMissingObject(refObject);
            }
        }
        
        // Find unexpected objects (in comparison but not in reference)  
        for (T compObject : comparisonObjects) {
            boolean found = false;
            for (T refObject : referenceObjects) {
                if (comparator.isSameObject(refObject, compObject, database, null)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("🔧 SnowflakeExtensionDiffGenerator: Found unexpected " + objectType.getSimpleName() + ": " + getObjectName(compObject));
                diffResult.addUnexpectedObject(compObject);
            }
        }
        
        // Find changed objects (same identity but different properties)
        for (T refObject : referenceObjects) {
            for (T compObject : comparisonObjects) {
                if (comparator.isSameObject(refObject, compObject, database, null)) {
                    // Objects are the same - check if properties differ
                    ObjectDifferences differences = comparator.findDifferences(refObject, compObject, database, compareControl, null, new java.util.HashSet<>());
                    if (differences != null && differences.hasDifferences()) {
                        System.out.println("🔧 SnowflakeExtensionDiffGenerator: Found changed " + objectType.getSimpleName() + ": " + getObjectName(compObject));
                        diffResult.addChangedObject(compObject, differences);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Get the appropriate comparator for the given extension object type.
     */
    private DatabaseObjectComparator getComparatorForType(Class<? extends DatabaseObject> objectType) {
        if (FileFormat.class.equals(objectType)) {
            return new FileFormatComparator();
        } else if (Warehouse.class.equals(objectType)) {
            return new WarehouseComparator();
        }
        
        // Fallback - create a basic comparator that only checks name equality
        return new DatabaseObjectComparator() {
            @Override
            public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database accordingTo, liquibase.diff.compare.DatabaseObjectComparatorChain chain) {
                String name1 = getObjectName(databaseObject1);
                String name2 = getObjectName(databaseObject2);
                return name1 != null && name1.equals(name2);
            }
            
            @Override
            public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database database, CompareControl compareControl, liquibase.diff.compare.DatabaseObjectComparatorChain chain, java.util.Set<String> exclude) {
                // Basic implementation - no differences detected
                return new ObjectDifferences(compareControl);
            }
            
            @Override
            public String[] hash(DatabaseObject databaseObject, Database accordingTo, liquibase.diff.compare.DatabaseObjectComparatorChain chain) {
                String name = getObjectName(databaseObject);
                return new String[] { name != null ? name : "unknown" };
            }
            
            @Override
            public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
                // Default priority for fallback comparator
                return PRIORITY_DEFAULT;
            }
        };
    }
    
    /**
     * Extract object name for logging purposes.
     */
    private String getObjectName(DatabaseObject obj) {
        try {
            return (String) obj.getClass().getMethod("getName").invoke(obj);
        } catch (Exception e) {
            return "unknown";
        }
    }
}