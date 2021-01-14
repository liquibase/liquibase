package liquibase.integration.ant;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.integration.ant.type.ChangeLogParametersType;
import liquibase.integration.ant.type.DatabaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.ui.UIFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Base class for all Ant Liquibase tasks.  This class sets up Liquibase and defines parameters
 * that are common to all tasks.
 */
public abstract class BaseLiquibaseTask extends Task {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    private final Map<String, Object> scopeValues = new HashMap<>();

    private AntClassLoader classLoader;
    private Liquibase liquibase;
    private ResourceAccessor resourceAccessor;

    private Path classpath;
    private DatabaseType databaseType;
    private ChangeLogParametersType changeLogParameters;
    private boolean promptOnNonLocalDatabase;

    public BaseLiquibaseTask() {
        super();
    }

    @Override
    public void init() throws BuildException {
        scopeValues.put(Scope.Attr.logService.name(), new AntTaskLogService(this));
        classpath = new Path(getProject());
    }

    @Override
    public final void execute() throws BuildException {
        super.execute();
        log(coreBundle.getString("starting.liquibase"), Project.MSG_INFO);
        classLoader = getProject().createClassLoader(classpath);
        classLoader.setParent(this.getClass().getClassLoader());
        classLoader.setThreadContextLoader();
        validateParameters();
        final Database[] database = {null};
        try {
            resourceAccessor = createResourceAccessor(classLoader);
            scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor);
            scopeValues.put(Scope.Attr.classLoader.name(), classLoader);

            Scope.child(scopeValues, () -> {
                Scope.getCurrentScope().getUI().setAllowPrompt(false);
                database[0] = createDatabaseFromType(databaseType, resourceAccessor);
                liquibase = new Liquibase(getChangeLogFile(), resourceAccessor, database[0]);
                if (changeLogParameters != null) {
                    changeLogParameters.applyParameters(liquibase);
                }
                if (isPromptOnNonLocalDatabase() && !liquibase.isSafeToRunUpdate() &&
                        UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
                    log("User chose not to run task against a non-local database.", Project.MSG_INFO);
                    return;
                }
                if (shouldRun()) {
                    executeWithLiquibaseClassloader();
                }
            });
        } catch (Exception e) {
            throw new BuildException("Unable to initialize Liquibase: " + e.getMessage(), e);
        } finally {
            closeDatabase(database[0]);
            classLoader.resetThreadContextLoader();
            classLoader.cleanup();
            classLoader = null;
        }
    }

    protected abstract void executeWithLiquibaseClassloader() throws BuildException;

    protected Database createDatabaseFromConfiguredDatabaseType() {
        return createDatabaseFromType(databaseType, getResourceAccessor());
    }

    protected Database createDatabaseFromType(DatabaseType databaseType, ResourceAccessor resourceAccessor) {
        return databaseType.createDatabase(resourceAccessor);
    }

    protected Liquibase getLiquibase() {
        return liquibase;
    }

    protected ResourceAccessor getResourceAccessor() {
        if (resourceAccessor == null) {
            throw new IllegalStateException("The ResourceAccessor has not been initialized. This usually means this " +
                    "method has been called before the task's execute method has called.");
        }
        return resourceAccessor;
    }

    /**
     * This method is designed to be overridden by subclasses when a change log is needed. By default it returns null.
     *
     * @return Returns null in this implementation. Subclasses that need a change log should implement.
     * @see AbstractChangeLogBasedTask#getChangeLogDirectory()
     */
    public String getChangeLogDirectory() {
        return null;
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
            log("Liquibase did not run because " + configuration.describeValueLookupLogic(globalConfiguration
                    .getProperty(GlobalConfiguration.SHOULD_RUN)) + " was set to false", Project.MSG_INFO);
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
        if (databaseType == null) {
            throw new BuildException("A database or databaseref is required.");
        }
    }

    /**
     * Creates a suitable ResourceAccessor for use in an Ant task..
     *
     * @param classLoader The ClassLoader to use in the ResourceAccessor. It is preferable that it is an AntClassLoader.
     * @return A ResourceAccessor.
     */
    private ResourceAccessor createResourceAccessor(AntClassLoader classLoader) {
        return new CompositeResourceAccessor(
                new AntResourceAccessor(classLoader, getChangeLogDirectory()),
                new ClassLoaderResourceAccessor(Thread.currentThread().getContextClassLoader())
        );
    }

    /*
     * Ant parameters
     */

    /**
     * Convenience method to safely close the database connection.
     *
     * @param database The database to close.
     */
    protected void closeDatabase(Database database) {
        try {
            if (database != null) {
                database.close();
            }
        } catch (DatabaseException e) {
            log("Error closing the database connection.", e, Project.MSG_WARN);
        }
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

    public void addDatabase(DatabaseType databaseType) {
        if (this.databaseType != null) {
            throw new BuildException("Only one <database> element is allowed.");
        }
        this.databaseType = databaseType;
    }

    public void setDatabaseRef(Reference databaseRef) {
        databaseType = new DatabaseType(getProject());
        databaseType.setRefid(databaseRef);
    }

    public void addChangeLogParameters(ChangeLogParametersType changeLogParameters) {
        if (this.changeLogParameters != null) {
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
}
