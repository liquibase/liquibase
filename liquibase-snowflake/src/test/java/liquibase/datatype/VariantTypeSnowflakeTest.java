package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.VariantTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class VariantTypeSnowflakeTest {

    VariantTypeSnowflake variantTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        variantTypeSnowflake = new VariantTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = variantTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("VARIANT", databaseDataType.getType());
        assertEquals("VARIANT", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(variantTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(variantTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, variantTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, variantTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(0, variantTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }

    @Test
    public void objectToSql_JsonString() {
        String jsonValue = "{\"name\": \"test\", \"value\": 123}";
        String result = variantTypeSnowflake.objectToSql(jsonValue, snowflakeDatabase);
        assertEquals("PARSE_JSON('{\"name\": \"test\", \"value\": 123}')", result);
    }

    @Test
    public void objectToSql_JsonArray() {
        String jsonValue = "[1, 2, 3]";
        String result = variantTypeSnowflake.objectToSql(jsonValue, snowflakeDatabase);
        assertEquals("PARSE_JSON('[1, 2, 3]')", result);
    }

    @Test
    public void objectToSql_NonJsonString() {
        String plainValue = "just a string";
        String result = variantTypeSnowflake.objectToSql(plainValue, snowflakeDatabase);
        assertNotEquals("PARSE_JSON('just a string')", result);
    }
}