package liquibase.migrator;

import junit.framework.TestCase;

import java.sql.Driver;
import java.sql.Connection;
import java.util.Properties;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    protected String changeLog;
    protected String driverName;
    protected String url;
    protected String driverDirectory;
    protected String username;
    protected String password;

    protected AbstractSimpleChangeLogRunnerTest(String changeLog, String driverDir, String driverName, String url) {
        this.changeLog = changeLog;
        this.driverName = driverName;
        this.url = url;
        this.driverDirectory = driverDir;
        username = "liquibase";
        password = "liquibase";
    }


    public void testRunChangeLog() throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator(changeLog, fileOpener);
        migrator.setContexts("test");
        migrator.setMode(Migrator.EXECUTE_MODE);

        Driver driver = (Driver) Class.forName(driverName, true, new JUnitJDBCDriverClassLoader(driverDirectory)).newInstance();
        Properties info = new Properties();
        info.put("user", username);
        info.put("password", password);
        Connection connection = driver.connect(url, info);
        migrator.init(connection);

        migrator.setShouldDropDatabaseObjectsFirst(true);
        migrator.migrate();

        //run again to test changelog testing logic
        migrator.setShouldDropDatabaseObjectsFirst(false);
        migrator.migrate();
    }

}
