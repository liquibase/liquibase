package liquibase.migrator.mssql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class MSSQL2kSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQL2kSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://windev1.sundog.net;instance=latest;DatabaseName=liquibase");
        this.username = "sundog";
        this.password = "sundog";
    }
}
