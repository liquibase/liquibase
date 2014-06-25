package liquibase.statement.core;

import liquibase.ExecutionEnvironment;
import liquibase.actiongenerator.ActionGeneratorFactory;
import liquibase.database.Database;
import  liquibase.ExecutionEnvironment;
import liquibase.statement.SqlStatement;
import liquibase.test.TestContext;
import org.junit.Test;

import static org.junit.Assert.fail;

public abstract class AbstractSqStatementTest<SqlStatementUnderTest extends SqlStatement> {

    protected abstract SqlStatementUnderTest createStatementUnderTest();

    @Test
    public void hasAtLeastOneGenerator() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (ActionGeneratorFactory.getInstance().supports(createStatementUnderTest(), new ExecutionEnvironment(database))) {
                return;
            };
        }
        fail("did not find a generator");
    }

}
