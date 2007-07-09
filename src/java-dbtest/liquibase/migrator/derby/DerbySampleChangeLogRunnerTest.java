package liquibase.migrator.derby;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.sql.SQLException;
import java.util.Properties;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class DerbySampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public DerbySampleChangeLogRunnerTest() throws Exception {
        super("derby",  "org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:liquibase;create=true");
    }

    protected void tearDown() throws Exception {
        try {
            driver.connect("jdbc:derby:liquibase;shutdown=true", new Properties());
        } catch (SQLException e) {
            ;//clean shutdown throws exception.
        }
        super.tearDown();
    }
}
