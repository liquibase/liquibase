package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.ant.logging.AntTaskLogFactory;
import liquibase.integration.ant.type.ChangeLogParametersType;
import liquibase.integration.ant.type.DatabaseType;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ui.UIFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.resources.FileResource;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Base class for all Ant Liquibase tasks.  This class sets up Liquibase and defines parameters
 * that are common to all tasks.
 */
public abstract class BaseLiquibaseTask extends Task {
    private AntClassLoader classLoader;
    private Liquibase liquibase;

    private Path classpath;
    private DatabaseType databaseType;
    private ChangeLogParametersType changeLogParameters;
    private boolean promptOnNonLocalDatabase = false;

    public BaseLiquibaseTask() {
        super();
    }

    @Override
    public void init() throws BuildException {
        LogFactory.setInstance(new AntTaskLogFactory(this));
        classpath = new Path(getProject());
    }

    @Override
    public final void execute() throws BuildException {
        super.execute();
        log("Starting Liquibase.", Project.MSG_INFO);
        classLoader = getProject().createClassLoader(classpath);
        classLoader.setParent(this.getClass().getClassLoader());
        classLoader.setThreadContextLoader();
        validateParameters();
        Database database = null;
        try {
            ResourceAccessor resourceAccessor = createResourceAccessor(classLoader);
            database = createDatabaseFromType(databaseType);
            liquibase = new Liquibase(getChangeLogFile(), resourceAccessor, database);
            if(changeLogParameters != null) {
                changeLogParameters.applyParameters(liquibase);
            }
            if (isPromptOnNonLocalDatabase() && !liquibase.isSafeToRunUpdate() &&
                    UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                log("User chose not to run task against a non-local database.", Project.MSG_INFO);
                return;
            }
            if(shouldRun()) {
                executeWithLiquibaseClassloader();
            }
        } catch (LiquibaseException e) {
            throw new BuildException("Unable to initialize Liquibase.", e);
        } finally {
            closeDatabase(database);
            classLoader.resetThreadContextLoader();
            classLoader.cleanup();
            classLoader = null;
        }
    }

    protected abstract void executeWithLiquibaseClassloader() throws BuildException;

    protected Database createDatabaseFromType(DatabaseType databaseType) {
        return databaseType.createDatabase(classLoader);
    }

    protected Liquibase getLiquibase() {
        return liquibase;
    }

    /**
     * This method is designed to be overridden by subclasses when a change log is needed. By default it returns null.
     *
     * @return Returns null in this implementation. Subclasses that need a change log should implement.
     * @see AbstractChangeLogBasedTask#getChangeLogFile()
     */
    protected String getChangeLogFile() {
        return null;
    }

    protected boolean shouldRun() {
        LiquibaseConfiguration configuration = LiquibaseConfiguration.getInstance();
        GlobalConfiguration globalConfiguration = configuration.getConfiguration(GlobalConfiguration.class);
        if (!globalConfiguration.getShouldRun()) {
            log("Liquibase did not run because " + configuration.describeValueLookupLogic(globalConfiguration.getProperty(GlobalConfiguration.SHOULD_RUN)) + " was set to false", Project.MSG_INFO);
            return false;
        }
        return true;
    }

    protected String getDefaultOutputEncoding() {
        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        GlobalConfiguration globalConfiguration = liquibaseConfiguration.getConfiguration(GlobalConfiguration.class);
        return globalConfiguration.getOutputEncoding();
    }

    /**
     * Subclasses that override this method must always call <code>super.validateParameters()</code> method.
     */
    protected void validateParameters() {
        if(databaseType == null) {
            throw new BuildException("A database or databaseref is required.");
        }
    }

    /**
     * Creates a suitable ResourceAccessor for use in an Ant task..
     *
     * @param classLoader The ClassLoader to use in the ResourceAccessor. It is preferable that it is an AntClassLoader.
     * @return A ResourceAccessor.
     */
    private ResourceAccessor createResourceAccessor(ClassLoader classLoader) {
        FileSystemResourceAccessor fileSystemResourceAccessor = new FileSystemResourceAccessor();
        ClassLoaderResourceAccessor classLoaderResourceAccessor = new ClassLoaderResourceAccessor(classLoader);
        return new CompositeResourceAccessor(fileSystemResourceAccessor, classLoaderResourceAccessor);
    }

    /**
     * Convenience method to safely close the database connection.
     *
     * @param database The database to close.
     */
    private void closeDatabase(Database database) {
        try {
            if(database != null) {
                database.close();
            }
        } catch (DatabaseException e) {
            log("Error closing the database connection.", e, Project.MSG_WARN);
        }
    }

    /*
     * Ant parameters
     */

    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void addDatabase(DatabaseType databaseType) {
        if(this.databaseType != null) {
            throw new BuildException("Only one <database> element is allowed.");
        }
        this.databaseType = databaseType;
    }

    public void setDatabaseRef(Reference databaseRef) {
        databaseType = new DatabaseType(getProject());
        databaseType.setRefid(databaseRef);
    }

    public void addChangeLogParameters(ChangeLogParametersType changeLogParameters) {
        if(this.changeLogParameters != null) {
            throw new BuildException("Only one <changeLogParameters> element is allowed.");
        }
        this.changeLogParameters = changeLogParameters;
    }

    public void setChangeLogParametersRef(Reference changeLogParametersRef) {
        changeLogParameters = new ChangeLogParametersType(getProject());
        changeLogParameters.setRefid(changeLogParametersRef);
    }

    public boolean isPromptOnNonLocalDatabase() {
        return promptOnNonLocalDatabase;
    }

    public void setPromptOnNonLocalDatabase(boolean promptOnNonLocalDatabase) {
        this.promptOnNonLocalDatabase = promptOnNonLocalDatabase;
    }

    /*************************
     * Deprecated parameters *
     *************************/

    /**
     * Helper method for deprecated ant attributes. This method will be removed when the deprecated methods are removed.
     * Do not rely on this method.
     *
     * @return DatabaseType object. Created if doesn't exist.
     */
    private DatabaseType getDatabaseType() {
        if(databaseType == null) {
            databaseType = new DatabaseType(getProject());
        }
        return databaseType;
    }

    /**
     * Helper method for deprecated ant attributes. This method will be removed when the deprecated methods are removed.
     * Do not rely on this method.
     *
     * @return ChangeLogParametersType
     */
    private ChangeLogParametersType getChangeLogParametersType() {
        if(changeLogParameters == null) {
            changeLogParameters = new ChangeLogParametersType(getProject());
        }
        return changeLogParameters;
    }

    /**
     * @deprecated Use {@link DatabaseType#getDriver()} instead.
     */
    @Deprecated
    public String getDriver() {
        return getDatabaseType().getDriver();
    }

    /**
     * @deprecated Use {@link DatabaseType#setDriver(String)} instead.
     */
    @Deprecated
    public void setDriver(String driver) {
        log("The driver attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setDriver(driver);
    }

    /**
     * @deprecated Use {@link DatabaseType#getUrl()} instead.
     */
    @Deprecated
    public String getUrl() {
        return getDatabaseType().getUrl();
    }

    /**
     * @deprecated Use {@link DatabaseType#setUrl(String)} instead.
     */
    @Deprecated
    public void setUrl(String url) {
        log("The url attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setUrl(url);
    }

    /**
     * @deprecated Use {@link DatabaseType#getUser()} instead.
     */
    @Deprecated
    public String getUsername() {
        return getDatabaseType().getUser();
    }

    /**
     * @deprecated Use {@link DatabaseType#setUser(String)} instead.
     */
    @Deprecated
    public void setUsername(String username) {
        log("The username attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setUser(username);
    }

    /**
     * @deprecated Use {@link DatabaseType#getPassword()} instead.
     */
    @Deprecated
    public String getPassword() {
        return getDatabaseType().getPassword();
    }

    /**
     * @deprecated Use {@link DatabaseType#setPassword(String)} instead.
     */
    @Deprecated
    public void setPassword(String password) {
        log("The password attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setPassword(password);
    }

    public void setChangeLogFile(String changeLogFile) {
        // This method is deprecated. Use child implementation.
    }

    /**
     * @deprecated Use {@link DatabaseType#getCurrentDateTimeFunction()} instead.
     */
    @Deprecated
    public String getCurrentDateTimeFunction() {
        return getDatabaseType().getCurrentDateTimeFunction();
    }

    /**
     * @deprecated Use {@link DatabaseType#setCurrentDateTimeFunction(String)} instead.
     */
    @Deprecated
    public void setCurrentDateTimeFunction(String currentDateTimeFunction) {
        log("The currentDateTimeFunction attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setCurrentDateTimeFunction(currentDateTimeFunction);
    }

    /**
     * This method does nothing. Use child implementations.
     */
    public FileResource getOutputFile() {
        // This method is deprecated. Use child implementations.
        return null;
    }

    /**
     * This method does nothing. Use child implementations.
     */
    public void setOutputFile(FileResource outputFile) {
        // This method is deprecated. Use child implementation.
    }

    /**
     * @deprecated Subclasses of this class should either instantiate their own output writers or use
     * {@link liquibase.integration.ant.AbstractChangeLogBasedTask} if their task involves a change log.
     */
    @Deprecated
    public Writer createOutputWriter() throws IOException {
        if (getOutputFile() == null) {
            return null;
        }
        GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class);
        String encoding = globalConfiguration.getOutputEncoding();
        return new OutputStreamWriter(getOutputFile().getOutputStream(), encoding);
    }

    /**
     * @deprecated Subclasses of this class should either instantiate their own output writers or use
     * {@link liquibase.integration.ant.AbstractChangeLogBasedTask} if the task involves a change log.
     */
    @Deprecated
    public PrintStream createPrintStream() throws IOException {
        if (getOutputFile() == null) {
            return null;
        }
        GlobalConfiguration globalConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class);
        String encoding = globalConfiguration.getOutputEncoding();
        return new PrintStream(getOutputFile().getOutputStream(), false, encoding);
    }

    public void setOutputEncoding(String outputEncoding) {
        // This method is deprecated. Use child implementation.
    }

    /**
     * @deprecated Use {@link DatabaseType#getDefaultCatalogName()} instead.
     */
    @Deprecated
    public String getDefaultCatalogName() {
        return getDatabaseType().getDefaultCatalogName();
    }

    /**
     * @deprecated Use {@link DatabaseType#setDefaultCatalogName(String)} instead.
     */
    @Deprecated
    public void setDefaultCatalogName(String defaultCatalogName) {
        log("The defaultCatalogName attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setDefaultCatalogName(defaultCatalogName);
    }

    /**
     * @deprecated Use {@link DatabaseType#getDefaultSchemaName()} instead.
     */
    @Deprecated
    public String getDefaultSchemaName() {
        return getDatabaseType().getDefaultSchemaName();
    }

    /**
     * @deprecated Use {@link DatabaseType#setDefaultSchemaName(String)} instead.
     */
    @Deprecated
    public void setDefaultSchemaName(String defaultSchemaName) {
        log("The driver attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setDefaultSchemaName(defaultSchemaName);
    }

    /**
     * @deprecated Use {@link ChangeLogParametersType#addConfiguredChangeLogParameter(Property)} instead.
     */
    @Deprecated
    public void addConfiguredChangeLogProperty(ChangeLogProperty changeLogProperty) {
        log("The <changeLogProperty> element is deprecated. Use a nested <changeLogParameters> element instead.", Project.MSG_WARN);
        Property property = new Property();
        property.setName(changeLogProperty.getName());
        property.setValue(changeLogProperty.getValue());
        getChangeLogParametersType().addConfiguredChangeLogParameter(property);
    }

    /**
     * @deprecated The Liquibase class is now created automatically when the Ant task is executed. Use
     * {@link #getLiquibase()} instead.
     */
    @Deprecated
    protected Liquibase createLiquibase() throws Exception {
        return this.liquibase;
    }

    /**
     * @deprecated Use {@link #createDatabaseFromType(DatabaseType)} instead.
     */
    @Deprecated
    protected Database createDatabaseObject(String driverClassName,
                                            String databaseUrl,
                                            String username,
                                            String password,
                                            String defaultCatalogName,
                                            String defaultSchemaName,
                                            String databaseClass) throws Exception {
        return createDatabaseFromType(databaseType);
    }

    /**
     * This method no longer does anything. Please extend from
     * {@link liquibase.integration.ant.AbstractChangeLogBasedTask} which has the equivalent method.
     */
    public String getContexts() {
        return null;
    }

    /**
     * This method no longer does anything. Please extend from
     * {@link liquibase.integration.ant.AbstractChangeLogBasedTask} which has the equivalent method.
     */
    public void setContexts(String cntx) {
        // Parent method is deprecated. Use child implementations.
    }


    /**
     * Redirector of logs from java.util.logging to ANT's logging
     */
    @Deprecated
    protected static class LogRedirector {

        private final Task task;

        /**
         * Constructor
         *
         * @param task Ant task
         */
        protected LogRedirector(Task task) {
            super();
            this.task = task;
        }

        protected void redirectLogger() {
            registerHandler(createHandler());
        }

        protected void registerHandler(Handler theHandler) {
            Logger logger = LogFactory.getInstance().getLog();
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

    /**
     * @deprecated Use {@link #closeDatabase(liquibase.database.Database)} instead.
     */
    @Deprecated
    protected void closeDatabase(Liquibase liquibase) {
        if (liquibase != null && liquibase.getDatabase() != null && liquibase.getDatabase().getConnection() != null) {
            try {
                liquibase.getDatabase().close();
            } catch (DatabaseException e) {
                log("Error closing database: " + e.getMessage());
            }
        }
    }

    /**
     * @deprecated Use {@link DatabaseType#getDatabaseClass()} instead.
     */
    @Deprecated
    public String getDatabaseClass() {
        return getDatabaseType().getDatabaseClass();
    }

    /**
     * @deprecated Use {@link DatabaseType#setDatabaseClass(String)} instead.
     */
    @Deprecated
    public void setDatabaseClass(String databaseClass) {
        log("The databaseClass attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setDatabaseClass(databaseClass);
    }

    /**
     * @deprecated Use {@link DatabaseType#getDatabaseChangeLogTableName()} instead.
     */
    @Deprecated
    public String getDatabaseChangeLogTableName() {
        return getDatabaseType().getDatabaseChangeLogTableName();
    }

    /**
     * @deprecated Use {@link DatabaseType#setDatabaseChangeLogTableName(String)} instead.
     */
    @Deprecated
    public void setDatabaseChangeLogTableName(String tableName) {
        log("The databaseChangeLogTableName attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setDatabaseChangeLogTableName(tableName);
    }

    /**
     * @deprecated Use {@link DatabaseType#getDatabaseChangeLogLockTableName()} instead.
     */
    @Deprecated
    public String getDatabaseChangeLogLockTableName() {
        return getDatabaseType().getDatabaseChangeLogLockTableName();
    }

    @Deprecated
    public void setDatabaseChangeLogLockTableName(String tableName) {
        log("The databaseChangeLogLockTableName attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        databaseType.setDatabaseChangeLogLockTableName(tableName);
    }

    /**
     * @deprecated Use {@link DatabaseType#getLiquibaseTablespaceName()} instead.
     */
    @Deprecated
    public String getDatabaseChangeLogObjectsTablespace() {
        return databaseType.getLiquibaseTablespaceName();
    }

    /**
     * @deprecated Use {@link DatabaseType#setLiquibaseTablespaceName(String)} instead.
     */
    @Deprecated
    public void setDatabaseChangeLogObjectsTablespace(String tablespaceName) {
        log("The databaseChangeLogObjectsTablespace attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setLiquibaseTablespaceName(tablespaceName);
    }

    /**
     * @deprecated Use {@link DatabaseType#isOutputDefaultSchema()} instead.
     */
    @Deprecated
    public boolean isOutputDefaultSchema() {
        return getDatabaseType().isOutputDefaultSchema();
    }

    /**
     * If not set, defaults to true.
     *
     * @deprecated Use a nested {@link DatabaseType DatabaseType} instead.
     * @param outputDefaultSchema True to output the default schema.
     */
    @Deprecated
    public void setOutputDefaultSchema(boolean outputDefaultSchema) {
        log("The outputDefaultSchema attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setOutputDefaultSchema(outputDefaultSchema);
    }

    /**
     * @deprecated Use {@link DatabaseType#isOutputDefaultCatalog()} instead.
     */
    @Deprecated
    public boolean isOutputDefaultCatalog() {
        return getDatabaseType().isOutputDefaultCatalog();
    }

    /**
     * If not set, defaults to true
     *
     * @deprecated Use {@link DatabaseType#setOutputDefaultCatalog(boolean)} instead.
     * @param outputDefaultCatalog True to output the default catalog.
     */
    @Deprecated
    public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
        log("The outputDefaultCatalog attribute is deprecated. Use a nested <database> element or set the databaseRef attribute instead.", Project.MSG_WARN);
        getDatabaseType().setOutputDefaultCatalog(outputDefaultCatalog);
    }

    /**
     * @deprecated No longer needed. This method has no replacement.
     * @return Log level.
     */
    @Deprecated
    public String getLogLevel() {
        return LogFactory.getInstance().getLog().getLogLevel().name();
    }

    /**
     * @deprecated Use the ant logging flags (-debug, -verbose, -quiet) instead of this method to control logging
     * output. This will no longer change log levels.
     * @param level Log level to set.
     */
    @Deprecated
    public void setLogLevel(String level) {
        LogFactory.getInstance().getLog().setLogLevel(level);
    }

    /**
     * @deprecated Use {@link ChangeLogParametersType} instead.
     */
    @Deprecated
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
