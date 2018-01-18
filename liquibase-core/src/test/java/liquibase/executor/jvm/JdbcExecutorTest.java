package liquibase.executor.jvm;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.executor.ExecutorService;
import org.junit.Test;

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

        assertNotNull(ExecutorService.getInstance().getExecutor(oracle1));
        assertNotNull(ExecutorService.getInstance().getExecutor(oracle2));
        assertNotNull(ExecutorService.getInstance().getExecutor(mysql));

        assertTrue(ExecutorService.getInstance().getExecutor(oracle1) == ExecutorService.getInstance().getExecutor(oracle1));
        assertTrue(ExecutorService.getInstance().getExecutor(oracle2) == ExecutorService.getInstance().getExecutor(oracle2));
        assertTrue(ExecutorService.getInstance().getExecutor(mysql) == ExecutorService.getInstance().getExecutor(mysql));

        assertTrue(ExecutorService.getInstance().getExecutor(oracle1) != ExecutorService.getInstance().getExecutor(oracle2));
        assertTrue(ExecutorService.getInstance().getExecutor(oracle1) != ExecutorService.getInstance().getExecutor(mysql));
    }
}
