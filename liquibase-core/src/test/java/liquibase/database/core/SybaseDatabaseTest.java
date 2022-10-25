package liquibase.database.core;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Tests for {@link PostgresDatabase}
 * @author crenan
 */
public class SybaseDatabaseTest extends AbstractJdbcDatabaseTest {

    public SybaseDatabaseTest() throws Exception {
        super(new SybaseDatabase());
    }
    
    @Override
    protected String getProductNameString() {
        return "Sybase";
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("GETDATE()", getDatabase().getCurrentDateTimeFunction());
    }
    
    @Test
    public void testGetDefaultDriver() throws DatabaseException {
        try (Database database = new SybaseDatabase()) {
            assertEquals("com.sybase.jdbc4.jdbc.SybDriver", database.getDefaultDriver("jdbc:xsybase://localhost/liquibase"));
            assertEquals("com.sybase.jdbc4.jdbc.SybDriver", database.getDefaultDriver("jdbc:sybase:Tds://localhost/liquibase"));
            assertEquals("net.sourceforge.jtds.jdbc.Driver", database.getDefaultDriver("jdbc:jtds:sybase://localhost/liquibase"));

            assertNull(database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }
    
}
