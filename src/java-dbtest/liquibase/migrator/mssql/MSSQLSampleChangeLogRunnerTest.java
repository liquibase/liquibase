package liquibase.migrator.mssql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MSSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQLSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://localhost;databaseName=liquibase");
    }
}
