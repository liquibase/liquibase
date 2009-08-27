package liquibase.dbtest.mysql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;
import liquibase.exception.ValidationFailedException;
import liquibase.Liquibase;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() throws Exception {
        super("mysql", "jdbc:mysql://192.168.1.8/liquibase");
    }

    @Override
    public void testRollbackToChange() throws Exception {
        super.testRollbackToChange();
    }

    @Override
    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        super.testRollbackableChangeLogScriptOnExistingDatabase();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
