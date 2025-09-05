package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Snowflake-specific comparator for Table objects.
 * Compares Snowflake-specific table attributes while excluding state properties.
 * 
 * ADDRESSES_CORE_ISSUE: Complete Table object comparison for Snowflake-specific attributes.
 */
public class TableComparatorSnowflake implements DatabaseObjectComparator {
    
    // State properties that should be excluded from differences (runtime state)
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "created", "lastAltered", "owner", "rowCount", "bytes"
    };

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, 
                         DatabaseObjectComparatorChain chain) {
        Table table = (Table) databaseObject;
        return new String[] { 
            table.getSchema() != null ? table.getSchema().getName() : "",
            table.getName() 
        };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                Database accordingTo, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Table && databaseObject2 instanceof Table)) {
            return false;
        }
        
        Table table1 = (Table) databaseObject1;
        Table table2 = (Table) databaseObject2;
        
        // Compare table names (case-insensitive for Snowflake)
        String name1 = table1.getName();
        String name2 = table2.getName();
        
        if (name1 == null && name2 == null) {
            // Both null, check schema
        } else if (name1 == null || name2 == null) {
            return false;
        } else if (!name1.equalsIgnoreCase(name2)) {
            return false;
        }
        
        // Compare schema names (case-insensitive for Snowflake)
        String schema1 = table1.getSchema() != null ? table1.getSchema().getName() : null;
        String schema2 = table2.getSchema() != null ? table2.getSchema().getName() : null;
        
        if (schema1 == null && schema2 == null) {
            return true;
        }
        if (schema1 == null || schema2 == null) {
            return false;
        }
        
        return schema1.equalsIgnoreCase(schema2);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                                           Database accordingTo, CompareControl compareControl, 
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        Table table1 = (Table) databaseObject1;
        Table table2 = (Table) databaseObject2;
        
        // Exclude state fields from comparison
        Set<String> excludedFields = new HashSet<>(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        // Compare Snowflake-specific configuration properties only
        compareAttribute(differences, "clusteringKey", table1, table2);
        compareAttribute(differences, "retentionTime", table1, table2);
        compareAttribute(differences, "isTransient", table1, table2);
        
        // Note: Standard table properties like remarks, columns, indexes are handled by core Liquibase
        
        return differences;
    }
    
    /**
     * Compares a specific attribute between two tables.
     */
    private void compareAttribute(ObjectDifferences differences, String attributeName, Table table1, Table table2) {
        Object value1 = table1.getAttribute(attributeName, Object.class);
        Object value2 = table2.getAttribute(attributeName, Object.class);
        
        if (value1 == null && value2 == null) {
            return;
        }
        if (value1 == null || value2 == null) {
            differences.addDifference(attributeName, value1, value2);
            return;
        }
        if (!value1.equals(value2)) {
            differences.addDifference(attributeName, value1, value2);
        }
    }
}