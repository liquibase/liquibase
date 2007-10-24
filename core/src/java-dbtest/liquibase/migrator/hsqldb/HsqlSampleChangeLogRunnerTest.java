package liquibase.migrator.hsqldb;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.sql.Statement;
import java.sql.SQLException;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class HsqlSampleChangeLogRunnerTest  extends AbstractSimpleChangeLogRunnerTest {

    public HsqlSampleChangeLogRunnerTest() throws Exception {
        super("hsqldb", "jdbc:hsqldb:mem:liquibase");
    }

    protected void setUp() throws Exception {
        super.setUp();
        try {
            connection.createStatement().execute("CREATE SCHEMA LIQUIBASEB AUTHORIZATION DBA");
            connection.commit();
        } catch (SQLException e) {
            ; //already exists
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
