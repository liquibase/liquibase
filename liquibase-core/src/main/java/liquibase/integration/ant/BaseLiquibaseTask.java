package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.configuration.GlobalConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.Driver;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Base class for all Ant Liquibase tasks.  This class sets up Liquibase and defines parameters
 * that are common to all tasks.
 */
public abstract class BaseLiquibaseTask extends Task {
    private String changeLogFile;
    private String driver;
    private String url;
    private String username;
    private String password;
    protected Path classpath;
    private boolean promptOnNonLocalDatabase = false;
    private String currentDateTimeFunction;
    private String contexts;
    private String outputFile;
    private String defaultCatalogName;
    private String defaultSchemaName;
    private String databaseClass;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    private String databaseChangeLogObjectsTablespace;


    private Map<String, Object> changeLogProperties = new HashMap<String, Object>();

    public BaseLiquibaseTask() {
        super();
        new LogRedirector(this).redirectLogger();
    }

    @Override
    public final void execute() throws BuildException {
        super.execute();

        AntClassLoader loader = getProject().createClassLoader(classpath);
        loader.setParent(this.getClass().getClassLoader());
        loader.setThreadContextLoader();

        try {
            executeWithLiquibaseClassloader();
        } finally {
            loader.resetThreadContextLoader();
        }
    }

    protected abstract void executeWithLiquibaseClassloader() throws BuildException;

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
        this.driver = driver.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile.trim();
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
        this.currentDateTimeFunction = currentDateTimeFunction.trim();
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile.trim();
    }

    public Writer createOutputWriter() throws IOException {
        if (outputFile == null) {
            return null;
        }
        return new FileWriter(new File(getOutputFile()));
    }

    public PrintStream createPrintStream() throws IOException {
        if (outputFile == null) {
            return null;
        }
        return new PrintStream(new File(getOutputFile()));
    }

    public String getDefaultCatalogName() {
        return defaultCatalogName;
    }

    public void setDefaultCatalogName(String defaultCatalogName) {
        this.defaultCatalogName = defaultCatalogName;
    }

    public String getDefaultSchemaName() {
        return defaultSchemaName;
    }

    public void setDefaultSchemaName(String defaultSchemaName) {
        this.defaultSchemaName = defaultSchemaName.trim();
    }

    public void addConfiguredChangeLogProperty(ChangeLogProperty changeLogProperty) {
        changeLogProperties.put(changeLogProperty.getName(), changeLogProperty.getValue());
    }

    protected Liquibase createLiquibase() throws Exception {
        ResourceAccessor antFO = new AntResourceAccessor(getProject(), classpath);
        ResourceAccessor fsFO = new FileSystemResourceAccessor();

        Database database = createDatabaseObject(getDriver(), getUrl(), getUsername(), getPassword(), getDefaultCatalogName(), getDefaultSchemaName(), getDatabaseClass());

        String changeLogFile = null;
        if (getChangeLogFile() != null) {
            changeLogFile = getChangeLogFile().trim();
        }
        Liquibase liquibase = new Liquibase(changeLogFile, new CompositeResourceAccessor(antFO, fsFO), database);
        liquibase.setCurrentDateTimeFunction(currentDateTimeFunction);
        for (Map.Entry<String, Object> entry : changeLogProperties.entrySet()) {
            liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
        }

        return liquibase;
    }

    protected Database createDatabaseObject(String driverClassName,
                                            String databaseUrl,
                                            String username,
                                            String password,
                                            String defaultCatalogName,
                                            String defaultSchemaName,
                                            String databaseClass) throws Exception {
        String[] strings = classpath.list();

        final List<URL> taskClassPath = new ArrayList<URL>();
        for (String string : strings) {
            URL url = new File(string).toURL();
            taskClassPath.add(url);
        }

        URLClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                return new URLClassLoader(taskClassPath.toArray(new URL[taskClassPath.size()]), Database.class.getClassLoader());
            }
        });

        Database database;


        if (databaseClass != null) {
            try {
                DatabaseFactory.getInstance().register((Database) Class.forName(databaseClass, true, loader).newInstance());
            } catch (ClassCastException e) { //fails in Ant in particular
                DatabaseFactory.getInstance().register((Database) Class.forName(databaseClass).newInstance());
            }
        }

        if (driverClassName == null) {
            driverClassName = DatabaseFactory.getInstance().findDefaultDriver(databaseUrl);
        }

        if (driverClassName == null) {
            throw new DatabaseException("driver not specified and no default could be found for " + databaseUrl);
        }

        Driver driver = (Driver) Class.forName(driverClassName, true, loader).newInstance();

        Properties info = new Properties();
        if (username != null) {
            info.put("user", username);
        }
        if (password != null) {
            info.put("password", password);
        }
        Connection connection = driver.connect(databaseUrl, info);

        if (connection == null) {
            throw new DatabaseException("Connection could not be created to " + databaseUrl + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
        }

        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        database.setDefaultCatalogName(defaultCatalogName);
        database.setDefaultSchemaName(defaultSchemaName);

        if (getDatabaseChangeLogTableName() != null)
            database.setDatabaseChangeLogTableName(getDatabaseChangeLogTableName());

        if (getDatabaseChangeLogLockTableName() != null)
            database.setDatabaseChangeLogLockTableName(getDatabaseChangeLogLockTableName());

        if (getDatabaseChangeLogObjectsTablespace() != null)
            database.setLiquibaseTablespaceName(getDatabaseChangeLogObjectsTablespace());

        return database;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String cntx) {
        this.contexts = cntx.trim();
    }


    /**
     * Redirector of logs from java.util.logging to ANT's loggging
     */
    protected static class LogRedirector {

        private final Task task;

        /**
         * Constructor
         *
         * @param task
         */
        protected LogRedirector(Task task) {
            super();
            this.task = task;
        }

        protected void redirectLogger() {
            registerHandler(createHandler());
        }

        protected void registerHandler(Handler theHandler) {
            Logger logger = LogFactory.getLogger();
        }


        protected Handler createHandler() {
            return new Handler() {
                @Override
                public void publish(LogRecord logRecord) {
                    task.log(logRecord.getMessage(), mapLevelToAntLevel(logRecord.getLevel()));
                }

                @Override
                public void close() throws SecurityException {
                }

                @Override
                public void flush() {
                }

                protected int mapLevelToAntLevel(Level level) {
                    if (Level.ALL == level) {
                        return Project.MSG_INFO;
                    } else if (Level.SEVERE == level) {
                        return Project.MSG_ERR;
                    } else if (Level.WARNING == level) {
                        return Project.MSG_WARN;
                    } else if (Level.INFO == level) {
                        return Project.MSG_INFO;
                    } else {
                        return Project.MSG_VERBOSE;
                    }
                }
            };
        }

    }

    protected boolean shouldRun() {
        GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class);
        if (!globalConfiguration.getShouldRun()) {
            log("Liquibase did not run because " + LiquibaseConfiguration.getInstance().describeValueLookupLogic(globalConfiguration.getProperty(GlobalConfiguration.SHOULD_RUN)) + " was set to false");
            return false;
        }
        return true;
    }

    protected void closeDatabase(Liquibase liquibase) {
        if (liquibase != null && liquibase.getDatabase() != null && liquibase.getDatabase().getConnection() != null) {
            try {
                liquibase.getDatabase().close();
            } catch (DatabaseException e) {
                log("Error closing database: " + e.getMessage());
            }
        }
    }

    public String getDatabaseClass() {
        return databaseClass;
    }

    public void setDatabaseClass(String databaseClass) {
        this.databaseClass = databaseClass;
    }

    public String getDatabaseChangeLogTableName() {
        return databaseChangeLogTableName;
    }


    public void setDatabaseChangeLogTableName(String tableName) {
        this.databaseChangeLogTableName = tableName;
    }


    public String getDatabaseChangeLogLockTableName() {
        return databaseChangeLogLockTableName;
    }


    public void setDatabaseChangeLogLockTableName(String tableName) {
        this.databaseChangeLogLockTableName = tableName;
    }

    public String getDatabaseChangeLogObjectsTablespace() {
        return databaseChangeLogObjectsTablespace;
    }


    public void setDatabaseChangeLogObjectsTablespace(String tablespaceName) {
        this.databaseChangeLogObjectsTablespace = tablespaceName;
    }

    public String getLogLevel() {
        return LogFactory.getLogger().getLogLevel().name();
    }

    public void setLogLevel(String level) {
        LogFactory.getLogger().setLogLevel(level);
    }

    public static class ChangeLogProperty {
        private String name;
        private String value;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
