package liquibase.dbtest.h2;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class H2SampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public H2SampleChangeLogRunnerTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");
    }
}
