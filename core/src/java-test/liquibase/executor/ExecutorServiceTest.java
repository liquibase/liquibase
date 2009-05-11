package liquibase.executor;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import liquibase.database.Database;
import liquibase.database.OracleDatabase;
import liquibase.database.MySQLDatabase;

public class ExecutorServiceTest {

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

        assertNotNull(ExecutorService.getExecutor(oracle1));
        assertNotNull(ExecutorService.getExecutor(oracle2));
        assertNotNull(ExecutorService.getExecutor(mysql));

        assertTrue(ExecutorService.getExecutor(oracle1) == ExecutorService.getExecutor(oracle1));
        assertTrue(ExecutorService.getExecutor(oracle2) == ExecutorService.getExecutor(oracle2));
        assertTrue(ExecutorService.getExecutor(mysql) == ExecutorService.getExecutor(mysql));

        assertTrue(ExecutorService.getExecutor(oracle1) != ExecutorService.getExecutor(oracle2));
        assertTrue(ExecutorService.getExecutor(oracle1) != ExecutorService.getExecutor(mysql));
    }

    @Test
    public void executingByDefault() {
        assertTrue(ExecutorService.getExecutor(new OracleDatabase()).executesStatements());
    }
}
