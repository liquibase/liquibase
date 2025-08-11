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

}
