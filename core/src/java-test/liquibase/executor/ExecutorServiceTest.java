package liquibase.executor;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.MySQLDatabase;

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

        assertNotNull(ExecutorService.getInstance().getWriteExecutor(oracle1));
        assertNotNull(ExecutorService.getInstance().getWriteExecutor(oracle2));
        assertNotNull(ExecutorService.getInstance().getWriteExecutor(mysql));

        assertTrue(ExecutorService.getInstance().getWriteExecutor(oracle1) == ExecutorService.getInstance().getWriteExecutor(oracle1));
        assertTrue(ExecutorService.getInstance().getWriteExecutor(oracle2) == ExecutorService.getInstance().getWriteExecutor(oracle2));
        assertTrue(ExecutorService.getInstance().getWriteExecutor(mysql) == ExecutorService.getInstance().getWriteExecutor(mysql));

        assertTrue(ExecutorService.getInstance().getWriteExecutor(oracle1) != ExecutorService.getInstance().getWriteExecutor(oracle2));
        assertTrue(ExecutorService.getInstance().getWriteExecutor(oracle1) != ExecutorService.getInstance().getWriteExecutor(mysql));
    }

    @Test
    public void executingByDefault() {
        assertTrue(ExecutorService.getInstance().getWriteExecutor(new OracleDatabase()).executesStatements());
    }
}
