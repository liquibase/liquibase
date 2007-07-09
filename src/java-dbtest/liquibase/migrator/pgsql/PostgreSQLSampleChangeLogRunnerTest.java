package liquibase.migrator.pgsql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class PostgreSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public PostgreSQLSampleChangeLogRunnerTest() throws Exception {
        super("pgsql", "org.postgresql.Driver", "jdbc:postgresql://localhost/liquibase");
    }
}
