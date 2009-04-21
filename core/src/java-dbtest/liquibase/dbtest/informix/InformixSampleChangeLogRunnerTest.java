package liquibase.dbtest.informix;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class InformixSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public InformixSampleChangeLogRunnerTest() throws Exception {
        super("informix", "jdbc:informix-sqli://localhost:9088/liquibase:informixserver=ol_ids_1150_1;user=liquibase;password=liquibase");
    }
    
}
