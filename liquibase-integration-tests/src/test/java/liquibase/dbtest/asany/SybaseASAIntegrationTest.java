package liquibase.dbtest.asany;

import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawParameterizedSqlStatement;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

@Ignore("No test database implementation")
public class SybaseASAIntegrationTest extends AbstractIntegrationTest {

    public SybaseASAIntegrationTest() throws Exception {
        super( "asany", DatabaseFactory.getInstance().getDatabase("asany"));
    }

    @Override
    protected boolean shouldRollBack() {
        return false;
    }

    @Test
    public void testGeneratedColumn() throws Exception {
        // given
        assumeNotNull(getDatabase());
        clearDatabase();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", getDatabase())
                .execute(
                        new RawParameterizedSqlStatement("CREATE TABLE generated_test (height_cm numeric, height_stored numeric COMPUTE (height_cm / 2.54))"));

        // when
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .setOutput(baos)
                .execute();

        // then
        assertTrue(baos.toString().contains("COMPUTE (&quot;height_cm&quot;/2.54)"));
   }

}
