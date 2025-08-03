package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.TimestampTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class TimestampTypeSnowflakeTest {

    TimestampTypeSnowflake timestampTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        timestampTypeSnowflake = new TimestampTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void objectToSql() {
        String result = timestampTypeSnowflake.objectToSql("'2021-01-01 12:30:45'", snowflakeDatabase);
        assertEquals("TO_TIMESTAMP('''2021-01-01 12:30:45''')", result);
    }

    @Test
    public void supports() {
        assertTrue(timestampTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(timestampTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, timestampTypeSnowflake.getPriority());
    }
}