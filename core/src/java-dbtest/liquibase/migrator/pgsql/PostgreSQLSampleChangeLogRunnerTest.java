package liquibase.migrator.pgsql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class PostgreSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public PostgreSQLSampleChangeLogRunnerTest() throws Exception {
        super("pgsql", "jdbc:postgresql://localhost/liquibase");
    }
}
