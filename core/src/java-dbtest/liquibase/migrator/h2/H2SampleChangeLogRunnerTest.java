package liquibase.migrator.h2;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class H2SampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public H2SampleChangeLogRunnerTest() throws Exception {
        super("h2", "org.h2.Driver", "jdbc:h2:mem:liquibase");
        username="sa";
        password="";
    }
}
