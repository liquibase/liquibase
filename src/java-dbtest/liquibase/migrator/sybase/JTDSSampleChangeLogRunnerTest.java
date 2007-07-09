package liquibase.migrator.sybase;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class JTDSSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public JTDSSampleChangeLogRunnerTest() throws Exception {
        super("sybase", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sybase://localhost/nathan:5000");
    }
}
