package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.TimeTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class TimeTypeSnowflakeTest {

    TimeTypeSnowflake timeTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        timeTypeSnowflake = new TimeTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void objectToSql() {
        String result = timeTypeSnowflake.objectToSql("'12:30:45'", snowflakeDatabase);
        assertEquals("TO_TIME('''12:30:45''')", result);
    }

    @Test
    public void supports() {
        assertTrue(timeTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(timeTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, timeTypeSnowflake.getPriority());
    }
}