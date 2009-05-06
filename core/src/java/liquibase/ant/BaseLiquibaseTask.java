package liquibase.ant;

import liquibase.CompositeFileOpener;
import liquibase.FileOpener;
import liquibase.FileSystemFileOpener;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.HibernateDatabase;
import liquibase.exception.JDBCException;
import liquibase.log.LogFactory;
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
import java.util.logging.Logger;

/**
 * Base class for all Ant LiquiBase tasks.  This class sets up LiquiBase and defines parameters
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
    private String contexts;
    private String outputFile;
    private String defaultSchemaName;
    private String databaseClass;
    private String databaseChangeLogTableName;
    private String databaseChangeLogLockTableName;
    
    
    private Map<String, Object> changeLogProperties = new HashMap<String, Object>();

    public BaseLiquibaseTask() {
        super();
        new LogRedirector(this).redirectLogger();
    }

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
        FileOpener antFO = new AntFileOpener(getProject(), classpath);
        FileOpener fsFO = new FileSystemFileOpener();

        Database database = createDatabaseObject(getDriver(), getUrl(), getUsername(), getPassword(), getDefaultSchemaName(),getDatabaseClass());
        
        String changeLogFile = null;
        if (getChangeLogFile() != null) {
            changeLogFile = getChangeLogFile().trim();
        }
        Liquibase liquibase = new Liquibase(changeLogFile, new CompositeFileOpener(antFO, fsFO), database);
        liquibase.setCurrentDateTimeFunction(currentDateTimeFunction);
        for (Map.Entry<String, Object> entry : changeLogProperties.entrySet()) {
            liquibase.setChangeLogParameterValue(entry.getKey(), entry.getValue());
        }

        return liquibase;
    }

    protected Database createDatabaseObject(String driverClassName,
    										String databaseUrl,
    										String username,
    										String password,
    										String defaultSchemaName,
    										String databaseClass) throws Exception {
        String[] strings = classpath.list();

        final List<URL> taskClassPath = new ArrayList<URL>();
        for (String string : strings) {
            URL url = new File(string).toURL();
            taskClassPath.add(url);
        }

        URLClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            public URLClassLoader run() {
                return new URLClassLoader(taskClassPath.toArray(new URL[taskClassPath.size()]));
            }
        });
        
        Database database;
        
        if (databaseUrl.startsWith("hibernate:")) {
            database = new HibernateDatabase(databaseUrl.substring("hibernate:".length()));
        } else {
	        if (databaseClass != null) {
	        	  try {
	        		  DatabaseFactory.getInstance().addDatabaseImplementation((Database) Class.forName(databaseClass, true, loader).newInstance());
	        	  } catch (ClassCastException e) { //fails in Ant in particular
	        		  DatabaseFactory.getInstance().addDatabaseImplementation((Database) Class.forName(databaseClass).newInstance());
	        	  }
	        }
	
	        if (driverClassName == null) {
	            driverClassName = DatabaseFactory.getInstance().findDefaultDriver(databaseUrl);
	        }
	
	        if (driverClassName == null) {
	            throw new JDBCException("driver not specified and no default could be found for "+databaseUrl);
	        }
	
	        Driver driver = (Driver) Class.forName(driverClassName, true, loader).newInstance();
	
	        Properties info = new Properties();
	        info.put("user", username);
	        info.put("password", password);
	        Connection connection = driver.connect(databaseUrl, info);
	
	        if (connection == null) {
	            throw new JDBCException("Connection could not be created to " + databaseUrl + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
	        }
	
	        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
	        database.setDefaultSchemaName(defaultSchemaName);
        }
        
        if (getDatabaseChangeLogTableName() != null)
        	database.setDatabaseChangeLogTableName(getDatabaseChangeLogTableName());
        
        if (getDatabaseChangeLogLockTableName() != null)
        	database.setDatabaseChangeLogLockTableName(getDatabaseChangeLogLockTableName());
        
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
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
            }
            logger.addHandler(theHandler);
            logger.setUseParentHandlers(false);
        }


        protected Handler createHandler() {
            return new Handler() {
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
        String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            log("LiquiBase did not run because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return false;
        }
        return true;
    }

    protected void closeDatabase(Liquibase liquibase) {
        if (liquibase != null && liquibase.getDatabase() != null && liquibase.getDatabase().getConnection() != null) {
            try {
                liquibase.getDatabase().close();
            } catch (JDBCException e) {
                log("Error closing database: "+e.getMessage());
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


	public static class ChangeLogProperty  {
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
