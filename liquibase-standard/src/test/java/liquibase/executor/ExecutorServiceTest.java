package liquibase.executor;

import liquibase.Scope;
import liquibase.database.core.PostgresDatabase;
import liquibase.executor.jvm.JdbcExampleExecutor;
import liquibase.executor.jvm.JdbcExecutor;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutorServiceTest {


    private ExecutorService executorService;

    @Before
    public void setUp() {
        executorService = Scope.getCurrentScope().getSingleton(ExecutorService.class);
        executorService.reset();
    }

    @Test
    public void getExecutor() {

        assertThat(executorService.getExecutor("jdbc", new PostgresDatabase()))
                .isInstanceOf(JdbcExecutor.class)
                .isNotInstanceOf(JdbcExampleExecutor.class);

        assertThat(executorService.getExecutor("jdbc", new JdbcExampleExecutor.ExampleJdbcDatabase()))
                .isInstanceOf(JdbcExampleExecutor.class);

    }

    /**
     * Regression test for cross-module state corruption in parallel (-T) Maven reactor builds:
     * {@link ExecutorService#clearExecutor(String, liquibase.database.Database)} must only remove
     * the given database's override, unlike {@link ExecutorService#reset()} which (correctly, by
     * design) clears every database's entry. Command-step cleanUp() paths used to call the blanket
     * reset() on every command completion; since this factory is a single JVM-wide singleton
     * (since #7877), that wiped out other concurrently-running Maven modules' "jdbc"/"logging"
     * Executor overrides (e.g. the SQL-writing LoggingExecutor installed by updateSQL) too.
     */
    @Test
    public void clearExecutorOnlyAffectsGivenDatabase() {
        PostgresDatabase databaseA = new PostgresDatabase();
        PostgresDatabase databaseB = new PostgresDatabase();
        Executor overrideA = new JdbcExampleExecutor();
        Executor overrideB = new JdbcExampleExecutor();

        executorService.setExecutor("jdbc", databaseA, overrideA);
        executorService.setExecutor("jdbc", databaseB, overrideB);

        executorService.clearExecutor("jdbc", databaseA);

        assertThat(executorService.getExecutor("jdbc", databaseB)).isSameAs(overrideB);
        assertThat(executorService.getExecutor("jdbc", databaseA))
                .isNotSameAs(overrideA)
                .isInstanceOf(JdbcExecutor.class);
    }

}
