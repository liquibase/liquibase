package liquibase.database.core;

import static org.junit.jupiter.api.Assertions.assertNull;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UnsupportedDatabaseTest {

    @ParameterizedTest
    @ValueSource(strings = {
       "jdbc:oracle://localhost;databaseName=liquibase",
       "jdbc:db2://localhost;databaseName=liquibase",
       "jdbc:hsqldb://localhost;databaseName=liquibase",
       "jdbc:derby://localhost;databaseName=liquibase",
       "jdbc:sqlserver://localhost;databaseName=liquibase",
       "jdbc:postgresql://localhost;databaseName=liquibase",
    })
    public void testGetDefaultDriver(String url) throws DatabaseException {
        try (Database database = new UnsupportedDatabase()) {
            assertNull(database.getDefaultDriver(url));
        }
    }
}
