package liquibase.datatype;

import liquibase.database.core.OracleDatabase;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.core.BinaryTypeSnowflake;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE;
import static org.junit.Assert.*;

public class BinaryTypeSnowflakeTest {


    BinaryTypeSnowflake binaryTypeSnowflake;
    SnowflakeDatabase snowflakeDatabase;

    @Before
    public void setup() {
        binaryTypeSnowflake = new BinaryTypeSnowflake();
        snowflakeDatabase = new SnowflakeDatabase();
    }

    @Test
    public void toDatabaseDataType() {
        DatabaseDataType databaseDataType = binaryTypeSnowflake.toDatabaseDataType(snowflakeDatabase);
        assertEquals("BINARY", databaseDataType.getType());
        assertEquals("BINARY", databaseDataType.toSql());
    }

    @Test
    public void binaryAliases() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("binary", snowflakeDatabase);
        String[] aliases = liquibaseDataType.getAliases();
        assertEquals(12, aliases.length);
        List<String> snowflakeBinaryAliasList = Arrays.asList("longblob",
                "longvarbinary",
                "java.sql.Types.BLOB",
                "java.sql.Types.LONGBLOB",
                "java.sql.Types.LONGVARBINARY",
                "java.sql.Types.VARBINARY",
                "java.sql.Types.BINARY",
                "varbinary",
                "binary",
                "image",
                "tinyblob",
                "mediumblob");
        assertTrue(Arrays.asList(aliases).containsAll(snowflakeBinaryAliasList));
    }

    @Test
    public void blobConvertsToBinary() {
        LiquibaseDataType liquibaseDataType = DataTypeFactory.getInstance().fromDescription("binary", snowflakeDatabase);
        assertEquals("liquibase.datatype.core.BinaryTypeSnowflake", liquibaseDataType.getClass().getName());
    }

    @Test
    public void supports() {
        assertTrue(binaryTypeSnowflake.supports(snowflakeDatabase));
        assertFalse(binaryTypeSnowflake.supports(new OracleDatabase()));
    }

    @Test
    public void getPriority() {
        assertEquals(PRIORITY_DATABASE, binaryTypeSnowflake.getPriority());
    }

    @Test
    public void getMinParameters() {
        assertEquals(0, binaryTypeSnowflake.getMinParameters(snowflakeDatabase));
    }

    @Test
    public void getMaxParameters() {
        assertEquals(12, binaryTypeSnowflake.getMaxParameters(snowflakeDatabase));
    }


}
