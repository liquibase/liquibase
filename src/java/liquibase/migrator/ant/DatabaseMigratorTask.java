package liquibase.migrator.ant;

import liquibase.migrator.ant.AntFileOpener;
import liquibase.migrator.Migrator;
import liquibase.migrator.MigrationFailedException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import javax.swing.*;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.io.File;

public class DatabaseMigratorTask extends Task {
    private String migrationFiles;
    private boolean dropFirst = true;
    private String driver;
    private String url;
    private String username;
    private String password;
    private Path classpath;
    private boolean promptOnNonDevDatabase;
    private boolean rebuildDatabase;
    private String contexts;

    public boolean isPromptOnNonDevDatabase() {
        return promptOnNonDevDatabase;
    }

    public void setPromptOnNonDevDatabase(boolean promptOnNonDevDatabase) {
        this.promptOnNonDevDatabase = promptOnNonDevDatabase;
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

    public String getMigrationFiles() {
        return migrationFiles;
    }

    public void setMigrationFiles(String migrationFiles) {
        this.migrationFiles = migrationFiles;
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

    public void execute() throws BuildException {
        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            log("Migrator did not run because '"+Migrator.SHOULD_RUN_SYSTEM_PROPERTY+"' system property was set to false");
            return;
        }
       

//        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
//        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
//        System.setProperty("javax.xml.transform.TransformerFactory", "org.apache.xalan.processor.TransformerFactoryImpl");
//        System.setProperty("org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser");
        Connection connection = null;
        try {
            String[] strings = classpath.list();
            List<URL> taskClassPath = new ArrayList<URL>();
            for (int i = 0; i < strings.length; i++) {
                URL url = new File(strings[i]).toURL();
                taskClassPath.add(url);
            }
            Driver driver = (Driver) Class.forName(getDriver(), true, new URLClassLoader(taskClassPath.toArray(new URL[taskClassPath.size()]))).newInstance();

            Properties info = new Properties();
            info.put("user", getUsername());
            info.put("password", getPassword());
            connection = driver.connect(getUrl(), info);

            String[] migrationFiles = getMigrationFiles().split(",");
            for (String migrationFile : migrationFiles) {
                Migrator migrator = new Migrator(migrationFile.trim(), new AntFileOpener(getProject(), classpath));
                migrator.setContexts(getContexts());
                migrator.init(connection);

                if (isPromptOnNonDevDatabase() && !migrator.isSaveToRunMigration()) {
                    if (JOptionPane.showConfirmDialog(null, "You are running a database refactoring against a non-local database.\n" +
                            "Database URL is: " + migrator.getDatabase().getConnectionURL() + "\n" +
                            "Username is: " + migrator.getDatabase().getConnectionUsername() + "\n\n" +
                            "Area you sure you want to do this?",
                            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                    {
                        throw new MigrationFailedException("Chose not to run against non-production database");
                    }
                }

                if (isDropFirst()) {
                    migrator.setShouldDropDatabaseObjectsFirst(isRebuildDatabase());
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
                    throw new BuildException(e);
                }
            }
        }


    }
}
