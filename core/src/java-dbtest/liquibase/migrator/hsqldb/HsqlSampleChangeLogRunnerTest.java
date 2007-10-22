package liquibase.migrator.hsqldb;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.sql.Statement;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class HsqlSampleChangeLogRunnerTest  extends AbstractSimpleChangeLogRunnerTest {

    public HsqlSampleChangeLogRunnerTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }
}
