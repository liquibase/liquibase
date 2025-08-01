package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DatabaseComparator implements DatabaseObjectComparator {
    
    // State properties that should be excluded from differences (runtime state)
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "createdOn", "origin", "owner", "ownerRoleType", "retention_time", "kind",
        "isTransient", "isCurrent", "isDefault", "resourceMonitorName", "droppedOn",
        "lastAltered", "budget"
    };

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Database.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        liquibase.database.object.Database database = (liquibase.database.object.Database) databaseObject;
        return new String[] { database.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof liquibase.database.object.Database && 
              databaseObject2 instanceof liquibase.database.object.Database)) {
            return false;
        }
        
        liquibase.database.object.Database database1 = (liquibase.database.object.Database) databaseObject1;
        liquibase.database.object.Database database2 = (liquibase.database.object.Database) databaseObject2;
        
        String name1 = database1.getName();
        String name2 = database2.getName();
        
        if (name1 == null && name2 == null) {
            return true;
        }
        if (name1 == null || name2 == null) {
            return false;
        }
        
        return name1.equalsIgnoreCase(name2);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                           Database accordingTo, CompareControl compareControl, 
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        liquibase.database.object.Database database1 = (liquibase.database.object.Database) databaseObject1;
        liquibase.database.object.Database database2 = (liquibase.database.object.Database) databaseObject2;
        
        // Exclude state fields from comparison
        Set<String> excludedFields = new HashSet<>(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        // Compare configuration properties only
        compareField(differences, "comment", database1.getComment(), database2.getComment());
        compareField(differences, "dataRetentionTimeInDays", 
                    database1.getDataRetentionTimeInDays(), database2.getDataRetentionTimeInDays());
        compareField(differences, "maxDataExtensionTimeInDays", 
                    database1.getMaxDataExtensionTimeInDays(), database2.getMaxDataExtensionTimeInDays());
        compareField(differences, "transient", database1.getTransient(), database2.getTransient());
        compareField(differences, "defaultDdlCollation", 
                    database1.getDefaultDdlCollation(), database2.getDefaultDdlCollation());
        compareField(differences, "resourceMonitor", 
                    database1.getResourceMonitor(), database2.getResourceMonitor());
        
        return differences;
    }
    
    private void compareField(ObjectDifferences differences, String fieldName, Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return;
        }
        if (value1 == null || value2 == null) {
            differences.addDifference(fieldName, value1, value2);
            return;
        }
        if (!value1.equals(value2)) {
            differences.addDifference(fieldName, value1, value2);
        }
    }
}