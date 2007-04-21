package liquibase.migrator;

import junit.framework.TestCase;

import java.sql.Driver;
import java.sql.Connection;
import java.util.Properties;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    private String changeLog;
    private String driverName;
    private String url;
    private String driverDirectory;

    protected AbstractSimpleChangeLogRunnerTest(String changeLog, String driverDir, String driverName, String url) {
        this.changeLog = changeLog;
        this.driverName = driverName;
        this.url = url;
        this.driverDirectory = driverDir;
    }


    public void testRunChangeLog() throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator(changeLog, fileOpener);
        migrator.setContexts("test");
        migrator.setMode(Migrator.EXECUTE_MODE);

        Driver driver = (Driver) Class.forName(driverName, true, new JUnitJDBCDriverClassLoader(driverDirectory)).newInstance();
        Properties info = new Properties();
        info.put("user", "liquibase");
        info.put("password", "liquibase");
        Connection connection = driver.connect(url, info);
        migrator.init(connection);

        migrator.setShouldDropDatabaseObjectsFirst(true);
        migrator.migrate();

        //run again to test changelog testing logic
        migrator.setShouldDropDatabaseObjectsFirst(false);
        migrator.migrate();
    }

}
