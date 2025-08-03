package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Snowflake-specific sequence comparator.
 * Handles comparison of Snowflake sequence objects with proper case-insensitive comparison
 * and excludes state properties from difference detection.
 */
public class SequenceComparatorSnowflake implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (objectType.equals(Sequence.class) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database database, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject instanceof Sequence)) {
            return null;
        }
        
        Sequence sequence = (Sequence) databaseObject;
        
        // Use schema name and sequence name for hash, ensuring consistent case handling
        String schemaName = sequence.getSchema() != null ? sequence.getSchema().getName() : "";
        String sequenceName = sequence.getName();
        
        return new String[]{
            schemaName != null ? schemaName : "",
            sequenceName != null ? sequenceName : ""
        };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database database, DatabaseObjectComparatorChain chain) {
        if (!(databaseObject1 instanceof Sequence) || !(databaseObject2 instanceof Sequence)) {
            return false;
        }
        
        Sequence sequence1 = (Sequence) databaseObject1;
        Sequence sequence2 = (Sequence) databaseObject2;
        
        // Compare names case-insensitively (Snowflake default behavior)
        String name1 = sequence1.getName();
        String name2 = sequence2.getName();
        
        if (!Objects.equals(
            name1 != null ? name1.toUpperCase() : null,
            name2 != null ? name2.toUpperCase() : null
        )) {
            return false;
        }
        
        // Compare schema names case-insensitively
        String schema1 = sequence1.getSchema() != null ? sequence1.getSchema().getName() : null;
        String schema2 = sequence2.getSchema() != null ? sequence2.getSchema().getName() : null;
        
        return Objects.equals(
            schema1 != null ? schema1.toUpperCase() : null,
            schema2 != null ? schema2.toUpperCase() : null
        );
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2, Database database, CompareControl compareControl, DatabaseObjectComparatorChain chain, Set<String> exclude) {
        if (!(databaseObject1 instanceof Sequence) || !(databaseObject2 instanceof Sequence)) {
            return null;
        }
        
        Sequence sequence1 = (Sequence) databaseObject1;
        Sequence sequence2 = (Sequence) databaseObject2;
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        // Compare all sequence configuration properties
        compareProperty(differences, "startValue", sequence1.getStartValue(), sequence2.getStartValue());
        compareProperty(differences, "incrementBy", sequence1.getIncrementBy(), sequence2.getIncrementBy());
        compareProperty(differences, "minValue", sequence1.getMinValue(), sequence2.getMinValue());
        compareProperty(differences, "maxValue", sequence1.getMaxValue(), sequence2.getMaxValue());
        compareProperty(differences, "willCycle", sequence1.getWillCycle(), sequence2.getWillCycle());
        
        // Compare Snowflake-specific sequence properties
        compareProperty(differences, "ordered", sequence1.getOrdered(), sequence2.getOrdered());
        compareProperty(differences, "comment", getSequenceComment(sequence1), getSequenceComment(sequence2));
        
        return differences;
    }
    
    /**
     * Helper method to compare sequence properties and add differences when found.
     * Handles null values appropriately.
     */
    private void compareProperty(ObjectDifferences differences, String propertyName, Object value1, Object value2) {
        if (!Objects.equals(value1, value2)) {
            differences.addDifference(propertyName, value1, value2);
        }
    }
    
    /**
     * Helper method to get sequence comment from attributes.
     * Snowflake sequence comments are stored as attributes.
     */
    private String getSequenceComment(Sequence sequence) {
        if (sequence.getAttributes() != null) {
            Object comment = sequence.getAttribute("comment", String.class);
            return comment != null ? comment.toString() : null;
        }
        return null;
    }
}