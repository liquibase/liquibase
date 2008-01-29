package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MSSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQLSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "jdbc:sqlserver://localhost;databaseName=liquibase");
    }
}
