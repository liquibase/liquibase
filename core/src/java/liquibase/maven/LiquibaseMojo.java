package liquibase.maven;

import liquibase.CompositeFileOpener;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.tools.ant.BuildException;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Runs the LiquiBase Migrator through Maven
 */
public class LiquibaseMojo extends AbstractMojo {
    private String changeLogFile;
    private boolean dropFirst = true;
    private String driver;
    private String url;
    private String username;
    private String password;
//    private Path classpath;
    private boolean promptOnNonLocalDatabase;
    private boolean rebuildDatabase;
    private String contexts;

    public boolean isPromptOnNonLocalDatabase() {
        return promptOnNonLocalDatabase;
    }

    public void setPromptOnNonLocalDatabase(boolean promptOnNonLocalDatabase) {
        this.promptOnNonLocalDatabase = promptOnNonLocalDatabase;
    }

    public boolean isRebuildDatabase() {
        return rebuildDatabase;
    }

    public void setRebuildDatabase(boolean rebuildDatabase) {
        this.rebuildDatabase = rebuildDatabase;
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

//    public Path createClasspath() {
//        if (this.classpath == null) {
//            this.classpath = new Path(getProject());
//        }
//        return this.classpath.createPath();
//    }
//
//    public void setClasspathRef(Reference r) {
//        createClasspath().setRefid(r);
//    }

    public boolean isDropFirst() {
        return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
        this.dropFirst = dropFirst;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String cntx) {
        this.contexts = cntx;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            getLog().warn("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

//        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
//        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
//        System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
//        System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
        Connection connection = null;
        try {
            Driver driver = (Driver) Class.forName(getDriver(), true, getClass().getClassLoader()).newInstance();

            Properties info = new Properties();
            info.put("user", getUsername());
            info.put("password", getPassword());
            connection = driver.connect(getUrl(), info);

            if (connection == null) {
                throw new JDBCException("Connection could not be created to "+getUrl()+" with driver "+driver.getClass().getName()+".  Possibly the wrong driver for the given database URL");
            }

            String[] changeLogFiles = getChangeLogFile().split(",");
            for (String changeLogFile : changeLogFiles) {
                FileOpener mFO = new MavenFileOpener();
                FileOpener fsFO = new FileSystemFileOpener();
                
                
                Migrator migrator = new Migrator(changeLogFile.trim(), new CompositeFileOpener(mFO,fsFO));
                migrator.setContexts(getContexts());
                migrator.init(connection);

                if (isPromptOnNonLocalDatabase() && !migrator.isSafeToRunMigration()) {
                    if (migrator.swingPromptForNonLocalDatabase()) {
                        throw new LiquibaseException("Chose not to run against non-production database");
                    }
                }

                if (isDropFirst()) {
                    migrator.dropAll();
                }
                migrator.migrate();
            }
        } catch (Exception e) {
            throw new BuildException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }
    }
}
