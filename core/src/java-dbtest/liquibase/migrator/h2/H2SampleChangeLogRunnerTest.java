package liquibase.migrator.h2;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.sql.SQLException;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class H2SampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public H2SampleChangeLogRunnerTest() throws Exception {
        super("h2", "jdbc:h2:mem:liquibase");
    }

    protected void setUp() throws Exception {
        super.setUp();
        try {
            connection.createStatement().execute("CREATE SCHEMA LIQUIBASEB");
            connection.commit();
        } catch (SQLException e) {
            ; //already exists
        }
    }

    protected void tearDown() throws Exception {
//        connection.createStatement().execute("DROP SCHEMA LIQUIBASEB");
        super.tearDown();
    }
}
