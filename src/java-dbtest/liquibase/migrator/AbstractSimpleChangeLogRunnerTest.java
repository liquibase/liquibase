package liquibase.migrator;

import junit.framework.TestCase;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.StringWriter;

public abstract class AbstractSimpleChangeLogRunnerTest extends TestCase {

    private String completeChangeLog;
    private String rollbackChangeLog;
    protected String username;
    protected String password;
    protected String driverName;
    protected String url;
    protected String driverDirectory;

    protected AbstractSimpleChangeLogRunnerTest(String changelogDir, String driverDir, String driverName, String url) {
        this.completeChangeLog = "changelogs/"+changelogDir+"/complete/root.changelog.xml";
        this.rollbackChangeLog = "changelogs/"+changelogDir+"/rollback/rollbackable.changelog.xml";
        this.driverName = driverName;
        this.url = url;
        this.driverDirectory = driverDir;
        username = "liquibase";
        password = "liquibase";

        Logger.getLogger(Migrator.DEFAULT_LOG_NAME).setLevel(Level.OFF);
    }

    protected Migrator createMigrator(String changeLogFile) throws Exception {
        JUnitFileOpener fileOpener = new JUnitFileOpener();
        Migrator migrator = new Migrator(changeLogFile, fileOpener);
        migrator.setContexts("test");

        Driver driver = (Driver) Class.forName(driverName, true, new JUnitJDBCDriverClassLoader(driverDirectory)).newInstance();
        Properties info = new Properties();
        info.put("user", username);
        info.put("password", password);
        Connection connection = driver.connect(url, info);
        migrator.init(connection);
        return migrator;
    }

    public void testRunChangeLog() throws Exception {
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        //run again to test changelog testing logic
        migrator = createMigrator(completeChangeLog);
        migrator.migrate();
    }

    public void testOutputChangeLog() throws Exception {
        StringWriter output = new StringWriter();
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator = createMigrator(completeChangeLog);
        migrator.setOutputSQLWriter(output);
        migrator.setMode(Migrator.OUTPUT_SQL_MODE);
        migrator.migrate();

//        System.out.println(output.getBuffer().toString());
    }

    public void testRollbackableChangeLog() throws Exception {
        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.EXECUTE_MODE);
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.EXECUTE_MODE);
        migrator.migrate();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.EXECUTE_ROLLBACK_MODE);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();
    }

    public void testRollbackableChangeLogScriptOnExistingDatabase() throws Exception {
        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.EXECUTE_MODE);
        migrator.migrate();

        StringWriter writer = new StringWriter();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.OUTPUT_ROLLBACK_SQL_MODE);
        migrator.setOutputSQLWriter(writer);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

//        System.out.println("Rollback SQL for "+driverName+"\n\n"+writer.toString());
    }

    public void testRollbackableChangeLogScriptOnFutureDatabase() throws Exception {
        StringWriter writer = new StringWriter();

        Migrator migrator = createMigrator(rollbackChangeLog);
        migrator.dropAll();

        migrator = createMigrator(rollbackChangeLog);
        migrator.setMode(Migrator.OUTPUT_FUTURE_ROLLBACK_SQL_MODE);
        migrator.setOutputSQLWriter(writer);
        migrator.setRollbackToDate(new Date(0));
        migrator.migrate();

//        System.out.println("Rollback SQL for future "+driverName+"\n\n"+writer.toString());
    }

    public void testTag() throws Exception{
        Migrator migrator = createMigrator(completeChangeLog);
        migrator.dropAll();

        migrator= createMigrator(completeChangeLog);
        migrator.migrate();

        migrator.tag("Test Tag");
    }

}
