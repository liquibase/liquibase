package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.GeographyTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class GeographyTypeSnowflakeTest {

    GeographyTypeSnowflake geographyTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        geographyTypeSnowflake = new GeographyTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = geographyTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("GEOGRAPHY", databaseDataType.getType());
        assertEquals("GEOGRAPHY", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(geographyTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(geographyTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, geographyTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, geographyTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(1, geographyTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }

    @Test
    public void objectToSql_WktPoint() {
        String wktValue = "POINT(-122.35 37.55)";
        String result = geographyTypeSnowflake.objectToSql(wktValue, snowflakeDatabase);
        assertEquals("ST_GEOGFROMTEXT('POINT(-122.35 37.55)')", result);
    }

    @Test
    public void objectToSql_WktPolygon() {
        String wktValue = "POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))";
        String result = geographyTypeSnowflake.objectToSql(wktValue, snowflakeDatabase);
        assertEquals("ST_GEOGFROMTEXT('POLYGON((0 0, 1 0, 1 1, 0 1, 0 0))')", result);
    }

    @Test
    public void objectToSql_GeoJson() {
        String geoJsonValue = "{\"type\": \"Point\", \"coordinates\": [-122.35, 37.55]}";
        String result = geographyTypeSnowflake.objectToSql(geoJsonValue, snowflakeDatabase);
        assertEquals("ST_GEOGFROMTEXT('{\"type\": \"Point\", \"coordinates\": [-122.35, 37.55]}')", result);
    }

    @Test
    public void objectToSql_NonGeographyString() {
        String plainValue = "not geography data";
        String result = geographyTypeSnowflake.objectToSql(plainValue, snowflakeDatabase);
        assertNotEquals("ST_GEOGFROMTEXT('not geography data')", result);
    }
}