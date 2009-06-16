package liquibase.dbtest.mysql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;
import liquibase.exception.ValidationFailedException;
import liquibase.Liquibase;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() throws Exception {
        super("mysql", "jdbc:mysql://localhost/liquibase");
    }

    public void testRun() throws Exception {
        Liquibase liquibase = createLiquibase(completeChangeLog);
        try {
            liquibase.update(this.contexts);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.out);
            throw e;
        }

    }
}
