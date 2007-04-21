package liquibase.migrator.mysql;

import junit.framework.TestCase;
import liquibase.migrator.Migrator;
import liquibase.migrator.JUnitFileOpener;
import liquibase.migrator.JUnitJDBCDriverClassLoader;

import java.io.InputStream;
import java.sql.Driver;
import java.sql.Connection;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.Properties;

public class SampleChangeLogRunner extends TestCase {
    public void testRunChangeLog() throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator("changelogs/mysql.changelog.xml", fileOpener);
        migrator.setContexts("test");
        migrator.setMode(Migrator.EXECUTE_MODE);

        Driver driver = (Driver) Class.forName("com.mysql.jdbc.Driver", true, new JUnitJDBCDriverClassLoader("mysql-5.0.4")).newInstance();
        Properties info = new Properties();
        info.put("user", "liquibase");
        info.put("password", "liquibase");
        Connection connection = driver.connect("jdbc:mysql://localhost/liquibase", info);
        migrator.init(connection);

        migrator.migrate();

        //run again to test changelog testing logic
        migrator.migrate();
    }
}
