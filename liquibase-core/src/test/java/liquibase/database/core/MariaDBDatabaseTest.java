package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import liquibase.database.AbstractJdbcDatabaseTest;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for {@link MariaDBDatabase}
 */
public class MariaDBDatabaseTest extends AbstractJdbcDatabaseTest<MariaDBDatabase> {

    public MariaDBDatabaseTest() throws Exception {
        super(new MariaDBDatabase());
    }

    @Override
    protected String getProductNameString() {
        return "MariaDB";
    }

    @Override
    @Test
    public void supportsInitiallyDeferrableColumns() {
        assertFalse(getDatabase().supportsInitiallyDeferrableColumns());
    }

    @Override
    @Test
    public void getCurrentDateTimeFunction() {
        assertEquals("NOW()", getDatabase().getCurrentDateTimeFunction());
    }

    @Test
    public void testGetDefaultDriver() {
        assertEquals("org.mariadb.jdbc.Driver", this.database.getDefaultDriver("jdbc:mariadb://localhost/liquibase"));

        assertNull(this.database.getDefaultDriver("jdbc:db2://localhost;databaseName=liquibase"));
    }

    @Override
    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        " catalogName | schemaName | tableName | catalogName.tableName ",
    })
    public void escapeTableName_withSchema(String catalogName, String schemaName, String tableName, String expected) throws DatabaseException {
        final Database database = getDatabase();
        assertEquals(expected, database.escapeTableName(catalogName, schemaName, tableName));
    }

    @Test
    public void escapeStringForDatabase_withBackslashes() {
        assertEquals("\\\\0", database.escapeStringForDatabase("\\0"));
    }
}
