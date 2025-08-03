package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.ArrayTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class ArrayTypeSnowflakeTest {

    ArrayTypeSnowflake arrayTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        arrayTypeSnowflake = new ArrayTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = arrayTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("ARRAY", databaseDataType.getType());
        assertEquals("ARRAY", databaseDataType.toSql());
    }

    @Test
    public void toDatabaseDataType_WithElementType() {
        arrayTypeSnowflake.addParameter("STRING");
        DatabaseDataType databaseDataType = arrayTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("ARRAY(STRING)", databaseDataType.getType());
        assertEquals("ARRAY(STRING)", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(arrayTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(arrayTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, arrayTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, arrayTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(1, arrayTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }

    @Test
    public void objectToSql_ArrayLiteral() {
        String arrayValue = "[1, 2, 3, 4]";
        String result = arrayTypeSnowflake.objectToSql(arrayValue, snowflakeDatabase);
        assertEquals("PARSE_JSON('[1, 2, 3, 4]')", result);
    }

    @Test
    public void objectToSql_NonArrayString() {
        String plainValue = "not an array";
        String result = arrayTypeSnowflake.objectToSql(plainValue, snowflakeDatabase);
        assertNotEquals("PARSE_JSON('not an array')", result);
    }
}