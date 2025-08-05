package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.FileFormat;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.compare.DatabaseObjectComparator;
import liquibase.diff.compare.DatabaseObjectComparatorChain;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Snowflake FileFormat diff comparator.
 * Compares FileFormat objects for differences during diff operations.
 */
public class FileFormatComparator implements DatabaseObjectComparator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (FileFormat.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_TYPE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public String[] hash(DatabaseObject databaseObject, Database accordingTo, DatabaseObjectComparatorChain chain) {
        FileFormat fileformat = (FileFormat) databaseObject;
        return new String[] { fileformat.getName() };
    }

    @Override
    public boolean isSameObject(DatabaseObject databaseObject1, DatabaseObject databaseObject2, 
                               Database accordingTo, DatabaseObjectComparatorChain chain) {
        
        if (!(databaseObject1 instanceof FileFormat) || !(databaseObject2 instanceof FileFormat)) {
            return false;
        }

        FileFormat fileformat1 = (FileFormat) databaseObject1;
        FileFormat fileformat2 = (FileFormat) databaseObject2;

        // Use the FileFormat's equals method for identity comparison
        return fileformat1.equals(fileformat2);
    }

    @Override
    public ObjectDifferences findDifferences(DatabaseObject databaseObject1, DatabaseObject databaseObject2,
                                           Database accordingTo, CompareControl compareControl,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        ObjectDifferences differences = new ObjectDifferences(compareControl);
        
        if (!(databaseObject1 instanceof FileFormat) || !(databaseObject2 instanceof FileFormat)) {
            return differences;
        }

        FileFormat fileformat1 = (FileFormat) databaseObject1;
        FileFormat fileformat2 = (FileFormat) databaseObject2;

        // Compare all configuration properties
        compareProperty("formatType", fileformat1.getFormatType(), fileformat2.getFormatType(), differences);
        compareProperty("compression", fileformat1.getCompression(), fileformat2.getCompression(), differences);
        compareProperty("recordDelimiter", fileformat1.getRecordDelimiter(), fileformat2.getRecordDelimiter(), differences);
        compareProperty("fieldDelimiter", fileformat1.getFieldDelimiter(), fileformat2.getFieldDelimiter(), differences);
        compareProperty("quoteCharacter", fileformat1.getQuoteCharacter(), fileformat2.getQuoteCharacter(), differences);
        compareProperty("escapeCharacter", fileformat1.getEscapeCharacter(), fileformat2.getEscapeCharacter(), differences);
        compareProperty("dateFormat", fileformat1.getDateFormat(), fileformat2.getDateFormat(), differences);
        compareProperty("timestampFormat", fileformat1.getTimestampFormat(), fileformat2.getTimestampFormat(), differences);
        compareProperty("binaryFormat", fileformat1.getBinaryFormat(), fileformat2.getBinaryFormat(), differences);
        compareProperty("nullIf", fileformat1.getNullIf(), fileformat2.getNullIf(), differences);
        compareProperty("skipHeader", fileformat1.getSkipHeader(), fileformat2.getSkipHeader(), differences);
        compareProperty("skipBlankLines", fileformat1.getSkipBlankLines(), fileformat2.getSkipBlankLines(), differences);
        compareProperty("trimSpace", fileformat1.getTrimSpace(), fileformat2.getTrimSpace(), differences);
        compareProperty("emptyFieldAsNull", fileformat1.getEmptyFieldAsNull(), fileformat2.getEmptyFieldAsNull(), differences);
        compareProperty("errorOnColumnCountMismatch", fileformat1.getErrorOnColumnCountMismatch(), fileformat2.getErrorOnColumnCountMismatch(), differences);
        
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
}