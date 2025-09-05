package liquibase.datatype;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Minimal comprehensive test for new Snowflake data types:
 * 1. Happy path validation (covered by integration tests)
 * 2. Parameter variation testing
 * 3. Invalid combination prevention
 */
public class DataTypeValidationTest {

    private SnowflakeDatabase snowflakeDb;

    @Before
    public void setup() {
        snowflakeDb = new SnowflakeDatabase();
    }

    @Test
    public void testParameterVariations() {
        // ARRAY: Test both valid variations (no params, 1 param)
        ArrayTypeSnowflake array1 = new ArrayTypeSnowflake();
        assertEquals("ARRAY", array1.toDatabaseDataType(snowflakeDb).getType());
        
        ArrayTypeSnowflake array2 = new ArrayTypeSnowflake();
        array2.addParameter("STRING");
        assertEquals("ARRAY(STRING)", array2.toDatabaseDataType(snowflakeDb).getType());
        
        // All other types: Only no-parameter variation
        assertEquals("VARIANT", new VariantTypeSnowflake().toDatabaseDataType(snowflakeDb).getType());
        assertEquals("OBJECT", new ObjectTypeSnowflake().toDatabaseDataType(snowflakeDb).getType());
        assertEquals("GEOGRAPHY", new GeographyTypeSnowflake().toDatabaseDataType(snowflakeDb).getType());
        assertEquals("GEOMETRY", new GeometryTypeSnowflake().toDatabaseDataType(snowflakeDb).getType());
    }

    @Test 
    public void testInvalidCombinationsPrevented() {
        // Verify parameter constraints prevent invalid combinations
        assertEquals("VARIANT max params", 0, new VariantTypeSnowflake().getMaxParameters(snowflakeDb));
        assertEquals("ARRAY max params", 1, new ArrayTypeSnowflake().getMaxParameters(snowflakeDb));
        assertEquals("OBJECT max params", 0, new ObjectTypeSnowflake().getMaxParameters(snowflakeDb));
        assertEquals("GEOGRAPHY max params", 1, new GeographyTypeSnowflake().getMaxParameters(snowflakeDb));
        assertEquals("GEOMETRY max params", 1, new GeometryTypeSnowflake().getMaxParameters(snowflakeDb));
        
        // Verify database support constraints prevent wrong database usage
        liquibase.database.core.PostgresDatabase postgresDb = new liquibase.database.core.PostgresDatabase();
        assertFalse("VARIANT should not support PostgreSQL", new VariantTypeSnowflake().supports(postgresDb));
        assertFalse("ARRAY should not support PostgreSQL", new ArrayTypeSnowflake().supports(postgresDb));
        assertFalse("OBJECT should not support PostgreSQL", new ObjectTypeSnowflake().supports(postgresDb));
        assertFalse("GEOGRAPHY should not support PostgreSQL", new GeographyTypeSnowflake().supports(postgresDb));
        assertFalse("GEOMETRY should not support PostgreSQL", new GeometryTypeSnowflake().supports(postgresDb));
    }

    @Test
    public void testSpecialValueHandling() {
        // Test variations of value conversion
        VariantTypeSnowflake variant = new VariantTypeSnowflake();
        assertEquals("PARSE_JSON('{\"key\": \"value\"}')", variant.objectToSql("{\"key\": \"value\"}", snowflakeDb));
        assertEquals("PARSE_JSON('[1,2,3]')", variant.objectToSql("[1,2,3]", snowflakeDb));
        
        GeographyTypeSnowflake geography = new GeographyTypeSnowflake();
        assertEquals("ST_GEOGFROMTEXT('POINT(-122.35 37.55)')", geography.objectToSql("POINT(-122.35 37.55)", snowflakeDb));
        assertEquals("ST_GEOGFROMTEXT('{\"type\": \"Point\", \"coordinates\": [1,2]}')", geography.objectToSql("{\"type\": \"Point\", \"coordinates\": [1,2]}", snowflakeDb));
        
        GeometryTypeSnowflake geometry = new GeometryTypeSnowflake();
        assertEquals("ST_GEOMFROMTEXT('POINT(100 200)')", geometry.objectToSql("POINT(100 200)", snowflakeDb));
        
        // Test that non-matching inputs don't get converted
        assertNotEquals("PARSE_JSON('plain text')", variant.objectToSql("plain text", snowflakeDb));
        assertNotEquals("ST_GEOGFROMTEXT('plain text')", geography.objectToSql("plain text", snowflakeDb));
    }
}