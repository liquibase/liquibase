package liquibase.migrator.mssql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class JTDSSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public JTDSSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "jdbc:jtds:sqlserver://localhost;databaseName=liquibase");
    }
}
