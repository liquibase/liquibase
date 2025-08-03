package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.DoubleTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class DoubleTypeSnowflakeTest {

    DoubleTypeSnowflake doubleTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        doubleTypeSnowflake = new DoubleTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = doubleTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("FLOAT", databaseDataType.getType());
        assertEquals("FLOAT", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(doubleTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(doubleTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, doubleTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, doubleTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(2, doubleTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }
}