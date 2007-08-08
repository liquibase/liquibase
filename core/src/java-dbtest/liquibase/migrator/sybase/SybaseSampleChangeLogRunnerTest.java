package liquibase.migrator.sybase;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class SybaseSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public SybaseSampleChangeLogRunnerTest() throws Exception {
        super("sybase", "com.sybase.jdbc3.jdbc.SybDriver", "jdbc:sybase:Tds:nathan:5000/liquibase");
    }
}
