package liquibase.dbtest.mysql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;
import liquibase.exception.ValidationFailedException;
import liquibase.Liquibase;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() throws Exception {
        super("mysql", "jdbc:mysql://localhost/liquibase");
    }

    @Override
    public void testRollbackToChange() throws Exception {
        super.testRollbackToChange();
    }

}
