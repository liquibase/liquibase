package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Warehouse;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WarehouseComparator implements DatabaseObjectComparator {
    
    // State properties that should be excluded from differences (runtime state)
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "state", "startedClusters", "running", "queued", "isDefault", "isCurrent",
        "available", "provisioning", "quiescing", "other", "createdOn", 
        "resumedOn", "updatedOn", "owner", "ownerRoleType"
    };

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Warehouse.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        Warehouse warehouse = (Warehouse) databaseObject;
        return new String[] { warehouse.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Warehouse && databaseObject2 instanceof Warehouse)) {
            return false;
        }
        
        Warehouse warehouse1 = (Warehouse) databaseObject1;
        Warehouse warehouse2 = (Warehouse) databaseObject2;
        
        String name1 = warehouse1.getName();
        String name2 = warehouse2.getName();
        
        if (name1 == null || name2 == null) {
            return false;
        }
        
        // Handle case sensitivity
        if (accordingTo != null && !accordingTo.isCaseSensitive()) {
            return name1.equalsIgnoreCase(name2);
        } else {
            return name1.equals(name2);
        }
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                           Database accordingTo, CompareControl compareControl,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        // Add our excluded state fields to the exclusion set
        exclude = new HashSet<>(exclude);
        exclude.addAll(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        // If chain is available, use it
        if (chain != null) {
            return chain.findDifferences(
                databaseObject1, databaseObject2, accordingTo, compareControl, exclude
            );
        } else {
            // For unit testing without chain, do basic property comparison
            ObjectDifferences differences = new ObjectDifferences(compareControl);
            
            if (!(databaseObject1 instanceof Warehouse && databaseObject2 instanceof Warehouse)) {
                return differences;
            }
            
            Warehouse warehouse1 = (Warehouse) databaseObject1;
            Warehouse warehouse2 = (Warehouse) databaseObject2;
            
            // Compare configuration properties (not excluded state fields)
            compareProperty(differences, "type", warehouse1.getType(), warehouse2.getType());
            compareProperty(differences, "size", warehouse1.getSize(), warehouse2.getSize());
            compareProperty(differences, "minClusterCount", warehouse1.getMinClusterCount(), warehouse2.getMinClusterCount());
            compareProperty(differences, "maxClusterCount", warehouse1.getMaxClusterCount(), warehouse2.getMaxClusterCount());
            compareProperty(differences, "autoSuspend", warehouse1.getAutoSuspend(), warehouse2.getAutoSuspend());
            compareProperty(differences, "autoResume", warehouse1.getAutoResume(), warehouse2.getAutoResume());
            compareProperty(differences, "resourceMonitor", warehouse1.getResourceMonitor(), warehouse2.getResourceMonitor());
            compareProperty(differences, "comment", warehouse1.getComment(), warehouse2.getComment());
            compareProperty(differences, "enableQueryAcceleration", warehouse1.getEnableQueryAcceleration(), warehouse2.getEnableQueryAcceleration());
            compareProperty(differences, "queryAccelerationMaxScaleFactor", warehouse1.getQueryAccelerationMaxScaleFactor(), warehouse2.getQueryAccelerationMaxScaleFactor());
            compareProperty(differences, "scalingPolicy", warehouse1.getScalingPolicy(), warehouse2.getScalingPolicy());
            compareProperty(differences, "resourceConstraint", warehouse1.getResourceConstraint(), warehouse2.getResourceConstraint());
            
            return differences;
        }
    }
    
    private void compareProperty(ObjectDifferences differences, String propertyName, Object value1, Object value2) {
        if (!java.util.Objects.equals(value1, value2)) {
            differences.addDifference(propertyName, value1, value2);
        }
    }
}