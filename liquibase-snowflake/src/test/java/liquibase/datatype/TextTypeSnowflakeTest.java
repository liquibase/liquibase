package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.TextTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class TextTypeSnowflakeTest {

    TextTypeSnowflake textTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        textTypeSnowflake = new TextTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = textTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("TEXT", databaseDataType.getType());
        assertEquals("TEXT", databaseDataType.toSql());
    }

    @Test
    public void supports() {
        assertTrue(textTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(textTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, textTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, textTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(0, textTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }
}