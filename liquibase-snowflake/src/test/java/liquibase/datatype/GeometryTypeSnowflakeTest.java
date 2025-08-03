package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.GeometryTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class GeometryTypeSnowflakeTest {

    GeometryTypeSnowflake geometryTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        geometryTypeSnowflake = new GeometryTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = geometryTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("GEOMETRY", databaseDataType.getType());
        assertEquals("GEOMETRY", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(geometryTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(geometryTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, geometryTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, geometryTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(1, geometryTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }

    @Test
    public void objectToSql_WktPoint() {
        String wktValue = "POINT(100 200)";
        String result = geometryTypeSnowflake.objectToSql(wktValue, snowflakeDatabase);
        assertEquals("ST_GEOMFROMTEXT('POINT(100 200)')", result);
    }

    @Test
    public void objectToSql_WktLineString() {
        String wktValue = "LINESTRING(0 0, 1 1, 2 2)";
        String result = geometryTypeSnowflake.objectToSql(wktValue, snowflakeDatabase);
        assertEquals("ST_GEOMFROMTEXT('LINESTRING(0 0, 1 1, 2 2)')", result);
    }

    @Test
    public void objectToSql_WktMultiPoint() {
        String wktValue = "MULTIPOINT((0 0), (1 1))";
        String result = geometryTypeSnowflake.objectToSql(wktValue, snowflakeDatabase);
        assertEquals("ST_GEOMFROMTEXT('MULTIPOINT((0 0), (1 1))')", result);
    }

    @Test
    public void objectToSql_NonGeometryString() {
        String plainValue = "not geometry data";
        String result = geometryTypeSnowflake.objectToSql(plainValue, snowflakeDatabase);
        assertNotEquals("ST_GEOMFROMTEXT('not geometry data')", result);
    }
}