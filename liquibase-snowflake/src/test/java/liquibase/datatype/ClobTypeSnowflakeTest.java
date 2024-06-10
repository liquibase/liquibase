package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.ClobTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class ClobTypeSnowflakeTest {


    ClobTypeSnowflake clobTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        clobTypeSnowflake = new ClobTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = clobTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("VARCHAR", databaseDataType.getType());
        assertEquals("VARCHAR", databaseDataType.toSql());
    }

    @Test
    public void clobAliases() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("clob", snowflakeDatabase);
        String[] aliases = liquibaseDataType.getAliases();
        assertEquals(14, aliases.length);
        List<String> snowflakeBinaryAliasList = Arrays.asList("longvarchar", "text", "longtext", "java.sql.Types.LONGVARCHAR",
                "java.sql.Types.CLOB","nclob", "longnvarchar", "ntext", "java.sql.Types.LONGNVARCHAR", "java.sql.Types.NCLOB",
                "tinytext", "mediumtext", "long varchar", "long nvarchar");
        assertTrue(Arrays.asList(aliases).containsAll(snowflakeBinaryAliasList));
    }

    @Test
    public void blobConvertsToBinary() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("clob", snowflakeDatabase);
        assertEquals("liquibase.datatype.core.ClobTypeSnowflake", liquibaseDataType.getClass().getName());
    }

    @Test
    public void supports() {
        assertTrue(clobTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(clobTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, clobTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, clobTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(0, clobTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }


}
