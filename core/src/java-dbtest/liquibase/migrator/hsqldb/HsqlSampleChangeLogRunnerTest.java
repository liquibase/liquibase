package liquibase.migrator.hsqldb;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.sql.Statement;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class HsqlSampleChangeLogRunnerTest  extends AbstractSimpleChangeLogRunnerTest {

    public HsqlSampleChangeLogRunnerTest() throws Exception {
        super("hsqldb", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:liquibase");
        username="sa";
        password="";
    }


    protected void tearDown() throws Exception {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("SHUTDOWN");
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        super.tearDown();
    }
}
