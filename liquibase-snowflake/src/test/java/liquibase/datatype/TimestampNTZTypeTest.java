package liquibase.datatype;

import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.TimestampNTZTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class TimestampNTZTypeTest {

    TimestampNTZTypeSnowflake timestampNTZType;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        timestampNTZType = new TimestampNTZTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() throws Exception {
        DatabaseDataType databaseDataType = timestampNTZType.toDatabaseDataType(snowflakeDatabase);
        assertEquals("TIMESTAMP_NTZ", databaseDataType.getType());
        assertEquals("TIMESTAMP_NTZ", databaseDataType.toSql());
        assertFalse(databaseDataType.isAutoIncrement());
    }

    @Test
    public void timestampAliases() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("datetime", snowflakeDatabase);
        String[] aliases = liquibaseDataType.getAliases();
        assertEquals(2, aliases.length);
        assertTrue(Arrays.asList(aliases).contains("datetime"));
        assertTrue(Arrays.asList(aliases).contains("java.sql.Types.DATETIME"));
    }

    @Test
    public void datetimeConvertsToTimestamp() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("datetime", snowflakeDatabase);
        assertEquals("liquibase.datatype.core.TimestampNTZTypeSnowflake", liquibaseDataType.getClass().getName());
    }

    @Test
    public void supports() throws Exception {
        assertTrue(timestampNTZType.supports(snowflakeDatabase));
        assertFalse(timestampNTZType.supports(new PostgresDatabase()));
    }

    @Test
    public void getPriority() throws Exception {
        assertEquals(PRIORITY_DATABASE, timestampNTZType.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, timestampNTZType.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(0, timestampNTZType.getMinParameters(snowflakeDatabase));
    }

}
