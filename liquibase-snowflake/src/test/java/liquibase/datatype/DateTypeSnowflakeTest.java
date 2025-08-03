package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.DateTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class DateTypeSnowflakeTest {

    DateTypeSnowflake dateTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        dateTypeSnowflake = new DateTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void objectToSql() {
        String result = dateTypeSnowflake.objectToSql("'2021-01-01'", snowflakeDatabase);
        assertEquals("TO_DATE('''2021-01-01''')", result);
    }

    @Test
    public void supports() {
        assertTrue(dateTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(dateTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, dateTypeSnowflake.getPriority());
    }
}