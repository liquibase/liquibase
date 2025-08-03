package liquibase.datatype;

import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Validate that SQL generation works correctly for complex scenarios
 */
public class SQLGenerationValidationTest {

    private SnowflakeDatabase database;

    @Before
    public void setup() {
        database = new SnowflakeDatabase();
    }

    @Test
    public void testVariantSQLGeneration() {
        VariantTypeSnowflake variantType = new VariantTypeSnowflake();
        
        // Test JSON object
        String jsonObject = "{\"name\": \"test\", \"value\": 123}";
        String result = variantType.objectToSql(jsonObject, database);
        assertEquals("PARSE_JSON('{\"name\": \"test\", \"value\": 123}')", result);
        
        // Test JSON array
        String jsonArray = "[1, 2, 3]";
        result = variantType.objectToSql(jsonArray, database);
        assertEquals("PARSE_JSON('[1, 2, 3]')", result);
        
        // Test string value containing single quotes
        String jsonWithQuotes = "{\"message\": \"It's a test\"}";
        result = variantType.objectToSql(jsonWithQuotes, database);
        assertEquals("PARSE_JSON('{\"message\": \"It''s a test\"}')", result);
    }

    @Test
    public void testArraySQLGeneration() {
        ArrayTypeSnowflake arrayType = new ArrayTypeSnowflake();
        
        // Test basic array
        String arrayValue = "[1, 2, 3, 4]";
        String result = arrayType.objectToSql(arrayValue, database);
        assertEquals("PARSE_JSON('[1, 2, 3, 4]')", result);
        
        // Test typed array
        arrayType.addParameter("STRING");
        DatabaseDataType dbType = arrayType.toDatabaseDataType(database);
        assertEquals("ARRAY(STRING)", dbType.getType());
    }

    @Test
    public void testGeographySQLGeneration() {
        GeographyTypeSnowflake geographyType = new GeographyTypeSnowflake();
        
        // Test WKT format
        String wktPoint = "POINT(-122.35 37.55)";
        String result = geographyType.objectToSql(wktPoint, database);
        assertEquals("ST_GEOGFROMTEXT('POINT(-122.35 37.55)')", result);
        
        // Test GeoJSON format (contains "coordinates")
        String geoJson = "{\"type\": \"Point\", \"coordinates\": [-122.35, 37.55]}";
        result = geographyType.objectToSql(geoJson, database);
        assertEquals("ST_GEOGFROMTEXT('{\"type\": \"Point\", \"coordinates\": [-122.35, 37.55]}')", result);
    }

    @Test
    public void testGeometrySQLGeneration() {
        GeometryTypeSnowflake geometryType = new GeometryTypeSnowflake();
        
        // Test WKT formats
        String wktPoint = "POINT(100 200)";
        String result = geometryType.objectToSql(wktPoint, database);
        assertEquals("ST_GEOMFROMTEXT('POINT(100 200)')", result);
        
        String wktLineString = "LINESTRING(0 0, 1 1, 2 2)";
        result = geometryType.objectToSql(wktLineString, database);
        assertEquals("ST_GEOMFROMTEXT('LINESTRING(0 0, 1 1, 2 2)')", result);
    }

    @Test
    public void testObjectSQLGeneration() {
        ObjectTypeSnowflake objectType = new ObjectTypeSnowflake();
        
        // Test object literal
        String objectValue = "{\"key1\": \"value1\", \"key2\": 42}";
        String result = objectType.objectToSql(objectValue, database);
        assertEquals("PARSE_JSON('{\"key1\": \"value1\", \"key2\": 42}')", result);
    }

    @Test
    public void testNonMatchingInputsReturnSuper() {
        VariantTypeSnowflake variantType = new VariantTypeSnowflake();
        GeographyTypeSnowflake geographyType = new GeographyTypeSnowflake();
        
        // Non-JSON string should not be converted to PARSE_JSON
        String plainString = "just a regular string";
        String variantResult = variantType.objectToSql(plainString, database);
        // Should call super.objectToSql(), which typically returns the input or processes it differently
        assertNotEquals("PARSE_JSON('just a regular string')", variantResult);
        
        // Non-geography string should not be converted to ST_GEOGFROMTEXT
        String geoResult = geographyType.objectToSql(plainString, database);
        assertNotEquals("ST_GEOGFROMTEXT('just a regular string')", geoResult);
    }
}