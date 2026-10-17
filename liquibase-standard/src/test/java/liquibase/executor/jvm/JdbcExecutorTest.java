package liquibase.executor.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.executor.ExecutorService;
import liquibase.logging.core.BufferedLogService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.statement.core.RawSqlStatement;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class JdbcExecutorTest {

    @Test
    public void getInstance() {
        final Database oracle1 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };
        final Database oracle2 = new OracleDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }

        };
        final Database mysql = new MySQLDatabase() {
            @Override
            public boolean equals(Object o) {
                return o == this;
            }
        };

        assertNotNull(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle1));
        assertNotNull(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle2));
        assertNotNull(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", mysql));

        assertSame(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle1), Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle1));
        assertSame(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle2), Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle2));
        assertSame(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", mysql), Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", mysql));

        assertNotSame(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle1), Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle2));
        assertNotSame(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", oracle1), Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", mysql));
    }

    @Test
    public void testGetErrorCode() {
        assertEquals("", new JdbcExecutor().getErrorCode(new RuntimeException()));
        assertEquals("(123) ", new JdbcExecutor().getErrorCode(new SQLException("reason", "sqlState", 123)));
        assertEquals("(0) ", new JdbcExecutor().getErrorCode(new SQLException()));
    }

    @Test
    public void queriesAndUpdatesAreLoggedAtSqlLogLevel() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:jdbcExecutorSqlLogging")) {
            H2Database database = new H2Database();
            database.setConnection(new JdbcConnection(connection));
            JdbcExecutor executor = new JdbcExecutor();
            executor.setDatabase(database);

            BufferedLogService logService = new BufferedLogService();
            Scope.child(Scope.Attr.logService, logService, () -> {
                executor.execute(new RawSqlStatement("CREATE TABLE sql_log_test (id INT)"));
                executor.update(new RawSqlStatement("INSERT INTO sql_log_test (id) VALUES (1)"));
                executor.queryForList(new RawSqlStatement("SELECT id FROM sql_log_test"));
                executor.execute(new RawParameterizedSqlStatement("UPDATE sql_log_test SET id = ?", 2));
                executor.queryForList(new RawParameterizedSqlStatement("SELECT COUNT(*) FROM sql_log_test WHERE id = ?", 2));
            });

            String log = logService.getLogAsString(Level.FINE);
            assertTrue(log, log.contains("CREATE TABLE sql_log_test (id INT)"));
            assertTrue(log, log.contains("INSERT INTO sql_log_test (id) VALUES (1)"));
            assertTrue(log, log.contains("SELECT id FROM sql_log_test"));
            assertTrue(log, log.contains("UPDATE sql_log_test SET id = ?"));
            assertTrue(log, log.contains("SELECT COUNT(*) FROM sql_log_test WHERE id = ?"));
        }
    }

}
