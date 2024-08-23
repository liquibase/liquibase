package liquibase.database;

import liquibase.database.jvm.JdbcConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionServiceFactoryTest {

    private String mockUrl = "mock://url";
    private ConnectionServiceFactory connectionServiceFactory;

    @BeforeEach
    public void setUp() {
        ConnectionServiceFactory.reset();
        connectionServiceFactory = ConnectionServiceFactory.getInstance();
    }

    @Test
    public void testGetDatabaseConnection() {

        // By Default only JdbcConnection registered that supports any Driver
        assertThat(connectionServiceFactory.getDatabaseConnection(mockUrl))
                .isInstanceOf(JdbcConnection.class);

        // Register Medium Priority Connection that supports the Driver
        final DatabaseConnection jdbcConnection100SupportsDriver = new JdbcConnection100SupportsDriver();
        connectionServiceFactory.register(jdbcConnection100SupportsDriver);

        assertThat(connectionServiceFactory.getDatabaseConnection(mockUrl))
                .isInstanceOf(JdbcConnection100SupportsDriver.class);

        // Register Higher Priority Connection that does not support the Driver
        final DatabaseConnection jdbcConnection200DoesNotSupportDriver = new JdbcConnection200DoesNotSupportDriver();
        connectionServiceFactory.register(jdbcConnection200DoesNotSupportDriver);

        assertThat(connectionServiceFactory.getDatabaseConnection(mockUrl))
                .isInstanceOf(JdbcConnection100SupportsDriver.class);

        // Register Lower Priority Connection that supports the Driver
        final DatabaseConnection jdbcConnection50SupportsDriver = new JdbcConnection50SupportsDriver();
        connectionServiceFactory.register(jdbcConnection50SupportsDriver);

        assertThat(connectionServiceFactory.getDatabaseConnection(mockUrl))
                .isInstanceOf(JdbcConnection100SupportsDriver.class);

    }

    /**
     * Test JdbcConnection Implementation Example Class with Low Priority and Supporting the Driver
     */
    private static class JdbcConnection50SupportsDriver extends JdbcConnection {

        @Override
        public int getPriority() {
            return 50;
        }

        @Override
        public boolean supports(String url) {
            return true;
        }
    }

    /**
     * Test JdbcConnection Implementation Example Class with Medium Priority and Supporting the Driver
     */
    private static class JdbcConnection100SupportsDriver extends JdbcConnection {

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public boolean supports(String url) {
            return true;
        }
    }

    /**
     * Test JdbcConnection Implementation Example Class with High Priority and Not Supporting the Driver
     */
    private static class JdbcConnection200DoesNotSupportDriver extends JdbcConnection {

        @Override
        public int getPriority() {
            return 200;
        }

        @Override
        public boolean supports(String url) {
            return false;
        }
    }
}
