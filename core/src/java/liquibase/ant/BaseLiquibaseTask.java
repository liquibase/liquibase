package liquibase.ant;

import liquibase.database.DatabaseFactory;
import liquibase.CompositeFileOpener;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.migrator.Migrator;
import liquibase.exception.JDBCException;
import liquibase.exception.MigrationFailedException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Base class for all Ant LiquiBase tasks.  This class sets up the migrator and defines parameters
 * that are common to all tasks.
 */
public class BaseLiquibaseTask extends Task {
    private String changeLogFile;
    private String driver;
    private String url;
    private String username;
    private String password;
    protected Path classpath;
    private boolean promptOnNonLocalDatabase = false;
    private String currentDateTimeFunction;

    public boolean isPromptOnNonLocalDatabase() {
        return promptOnNonLocalDatabase;
    }

    public void setPromptOnNonLocalDatabase(boolean promptOnNonLocalDatabase) {
        this.promptOnNonLocalDatabase = promptOnNonLocalDatabase;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

    public void setCurrentDateTimeFunction(String currentDateTimeFunction) {
        this.currentDateTimeFunction = currentDateTimeFunction;
    }

    protected Migrator createMigrator() throws MalformedURLException, ClassNotFoundException, JDBCException, SQLException, MigrationFailedException, IllegalAccessException, InstantiationException {
        FileOpener antFO = new AntFileOpener(getProject(), classpath);
        FileOpener fsFO = new FileSystemFileOpener();
        Migrator migrator = new Migrator(getChangeLogFile().trim(), new CompositeFileOpener(antFO,fsFO));

        String[] strings = classpath.list();
        final List<URL> taskClassPath = new ArrayList<URL>();
        for (int i = 0; i < strings.length; i++) {
            URL url = new File(strings[i]).toURL();
            taskClassPath.add(url);
        }

        URLClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            public URLClassLoader run() {
                return new URLClassLoader(taskClassPath.toArray(new URL[taskClassPath.size()]));
            }
        });

        String driverClassName = getDriver();
        if (driverClassName == null) {
            driverClassName = DatabaseFactory.getInstance().findDefaultDriver(getUrl());
        }

        if (driverClassName == null) {
            throw new JDBCException("driver not specified and no default could be found for the given url");
        }

        Driver driver = (Driver) Class.forName(driverClassName, true, loader).newInstance();

        Properties info = new Properties();
        info.put("user", getUsername());
        info.put("password", getPassword());
        Connection connection = driver.connect(getUrl(), info);

        if (connection == null) {
            throw new JDBCException("Connection could not be created to "+getUrl()+" with driver "+driver.getClass().getName()+".  Possibly the wrong driver for the given database URL");
        }

        migrator.init(connection);
        migrator.setCurrentDateTimeFunction(currentDateTimeFunction);

        return migrator;
    }

}
