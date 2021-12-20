package liquibase;

import liquibase.extension.testing.environment.TestEnvironment;
import liquibase.extension.testing.environment.TestEnvironmentFactory;
import org.junit.Rule;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class MyTest {

    @Rule
    public TestEnvironment mysql = TestEnvironmentFactory.getEnvironment("mysql");


    @Test
    public void runTest() throws SQLException {
        final Connection connection = mysql.openConnection();
        assert connection != null;
    }
}
