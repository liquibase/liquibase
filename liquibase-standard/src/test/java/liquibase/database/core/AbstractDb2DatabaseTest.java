package liquibase.database.core;

import liquibase.exception.DatabaseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AbstractDb2DatabaseTest {

    @Test
    public void testGetDateLiteral() throws DatabaseException {
        try (AbstractDb2Database database = new DB2Database()) {
            assertEquals("DATE('2018-12-31')", database.getDateLiteral("2018-12-31"));
            assertEquals("TIME('23:58:59')", database.getDateLiteral("23:58:59"));
            assertEquals("TIMESTAMP('2018-12-31 23:58:59')", database.getDateLiteral("2018-12-31 23:58:59"));
            assertEquals("UNSUPPORTED:foo", database.getDateLiteral("foo"));
        } catch (final DatabaseException e) {
            throw e;
        }
    }
}
