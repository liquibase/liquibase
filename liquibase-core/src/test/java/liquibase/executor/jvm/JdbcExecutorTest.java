package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.executor.ExecutorService;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

        assertNotNull(ExecutorService.getInstance().getExecutor("jdbc", oracle1));
        assertNotNull(ExecutorService.getInstance().getExecutor("jdbc", oracle2));
        assertNotNull(ExecutorService.getInstance().getExecutor("jdbc", mysql));

        assertTrue(ExecutorService.getInstance().getExecutor("jdbc", oracle1) == ExecutorService.getInstance().getExecutor("jdbc", oracle1));
        assertTrue(ExecutorService.getInstance().getExecutor("jdbc", oracle2) == ExecutorService.getInstance().getExecutor("jdbc", oracle2));
        assertTrue(ExecutorService.getInstance().getExecutor("jdbc", mysql) == ExecutorService.getInstance().getExecutor("jdbc", mysql));

        assertTrue(ExecutorService.getInstance().getExecutor("jdbc", oracle1) != ExecutorService.getInstance().getExecutor("jdbc", oracle2));
        assertTrue(ExecutorService.getInstance().getExecutor("jdbc", oracle1) != ExecutorService.getInstance().getExecutor("jdbc", mysql));
    }

    @Test
    public void testGetErrorCode() {
        assertEquals("", new JdbcExecutor().getErrorCode(new RuntimeException()));
        assertEquals("(123) ", new JdbcExecutor().getErrorCode(new SQLException("reason", "sqlState", 123)));
        assertEquals("(0) ", new JdbcExecutor().getErrorCode(new SQLException()));
    }

}
