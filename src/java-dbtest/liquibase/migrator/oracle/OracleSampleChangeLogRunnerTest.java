package liquibase.migrator.oracle;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class OracleSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public OracleSampleChangeLogRunnerTest() throws Exception {
        super("oracle", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost/XE");
    }
}
