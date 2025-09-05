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

public class SchemaComparator implements DatabaseObjectComparator {
    
    // State properties that should be excluded from differences (runtime state)
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "createdOn", "origin", "owner", "ownerRoleType", "retentionTime", "kind",
        "isTransient", "isCurrent", "isDefault", "resourceMonitorName", "droppedOn",
        "lastAltered", "budget", "databaseName"
    };

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Schema.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        liquibase.database.object.Schema schema = (liquibase.database.object.Schema) databaseObject;
        return new String[] { schema.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof liquibase.database.object.Schema && 
              databaseObject2 instanceof liquibase.database.object.Schema)) {
            return false;
        }
        
        liquibase.database.object.Schema schema1 = (liquibase.database.object.Schema) databaseObject1;
        liquibase.database.object.Schema schema2 = (liquibase.database.object.Schema) databaseObject2;
        
        String name1 = schema1.getName();
        String name2 = schema2.getName();
        
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
        
        liquibase.database.object.Schema schema1 = (liquibase.database.object.Schema) databaseObject1;
        liquibase.database.object.Schema schema2 = (liquibase.database.object.Schema) databaseObject2;
        
        // Exclude state fields from comparison
        Set<String> excludedFields = new HashSet<>(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        // Compare configuration properties only
        compareField(differences, "comment", schema1.getComment(), schema2.getComment());
        compareField(differences, "dataRetentionTimeInDays", 
                    schema1.getDataRetentionTimeInDays(), schema2.getDataRetentionTimeInDays());
        compareField(differences, "maxDataExtensionTimeInDays", 
                    schema1.getMaxDataExtensionTimeInDays(), schema2.getMaxDataExtensionTimeInDays());
        compareField(differences, "transient", schema1.getTransient(), schema2.getTransient());
        compareField(differences, "defaultDdlCollation", 
                    schema1.getDefaultDdlCollation(), schema2.getDefaultDdlCollation());
        compareField(differences, "managedAccess", schema1.getManagedAccess(), schema2.getManagedAccess());
        
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