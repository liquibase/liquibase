package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.ObjectTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class ObjectTypeSnowflakeTest {

    ObjectTypeSnowflake objectTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        objectTypeSnowflake = new ObjectTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = objectTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("OBJECT", databaseDataType.getType());
        assertEquals("OBJECT", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(objectTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(objectTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, objectTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, objectTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(0, objectTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }

    @Test
    public void objectToSql_ObjectLiteral() {
        String objectValue = "{\"key1\": \"value1\", \"key2\": 42}";
        String result = objectTypeSnowflake.objectToSql(objectValue, snowflakeDatabase);
        assertEquals("PARSE_JSON('{\"key1\": \"value1\", \"key2\": 42}')", result);
    }

    @Test
    public void objectToSql_NonObjectString() {
        String plainValue = "not an object";
        String result = objectTypeSnowflake.objectToSql(plainValue, snowflakeDatabase);
        assertNotEquals("PARSE_JSON('not an object')", result);
    }
}