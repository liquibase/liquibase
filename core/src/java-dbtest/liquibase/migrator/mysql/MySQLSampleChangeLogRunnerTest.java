package liquibase.migrator.mysql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() throws Exception {
        super("mysql", "jdbc:mysql://localhost/liquibase");
    }
}
