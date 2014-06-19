package liquibase.statement.core;

import liquibase.RuntimeEnvironment;
import liquibase.database.Database;
import liquibase.executor.ExecutionOptions;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import static org.junit.Assert.*;
import org.junit.Test;

public abstract class AbstractSqStatementTest<SqlStatementUnderTest extends SqlStatement> {

    protected abstract SqlStatementUnderTest createStatementUnderTest();

    @Test
    public void hasAtLeastOneGenerator() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (SqlGeneratorFactory.getInstance().supports(createStatementUnderTest(), new ExecutionOptions(new RuntimeEnvironment(database)))) {
                return;
            };
        }
        fail("did not find a generator");
    }

}
