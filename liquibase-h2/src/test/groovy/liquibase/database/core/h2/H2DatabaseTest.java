package liquibase.database.core.h2;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class H2DatabaseTest extends AbstractJdbcDatabaseTest {

    public H2DatabaseTest() throws Exception {
        super(new H2Database());
    }

    @Override
    protected String getProductNameString() {
        return "H2";
    }


    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        Assert.assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        Database database = getDatabase();

        assertEquals("org.h2.Driver", database.getDefaultDriver("jdbc:h2:mem:liquibase"));

        assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Override
    @Test
    public void escapeTableName_noSchema() {
        Database database = getDatabase();
        assertEquals("tableName", database.escapeObjectName("tableName", Table.class));
    }

    @Test
    public void escapeTableName_withSchema() {
        Database database = getDatabase();
        assertEquals("schemaName.tableName", database.escapeObjectName(new ObjectName("catalogName", "schemaName", "tableName"), Table.class));
    }    
}
